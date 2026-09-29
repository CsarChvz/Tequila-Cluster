package org.dev.tequilacluster.services.shared;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.alerts.ProcessAlertResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.ForbiddenStageActionException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * FR-44: alerts linked to a batch or a shipment. Every stage service calls
 * {@link #raiseForBatch} instead of writing to {@code process_alert} directly, so the panel and
 * the RB-001 "no open CRITICAL alert" gate ({@code BatchLifecycleService.complete}) stay
 * consistent.
 */
@Service
public class AlertService {

    private final ProcessAlertRepository processAlertRepository;
    private final BatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;
    private final StagePermissionService stagePermissionService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AlertService(
            ProcessAlertRepository processAlertRepository,
            BatchRepository batchRepository,
            ShipmentRepository shipmentRepository,
            AppUserRepository appUserRepository,
            StagePermissionService stagePermissionService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.processAlertRepository = processAlertRepository;
        this.batchRepository = batchRepository;
        this.shipmentRepository = shipmentRepository;
        this.appUserRepository = appUserRepository;
        this.stagePermissionService = stagePermissionService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public ProcessAlert raiseForBatch(UUID batchId, String alertType, AlertSeverity severity, String message) {
        Batch batchRef = batchRepository.getReferenceById(batchId);
        ProcessAlert alert = new ProcessAlert();
        alert.setBatch(batchRef);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setDetectedAt(Instant.now());
        return processAlertRepository.save(alert);
    }

    public ProcessAlert raiseForShipment(UUID shipmentId, String alertType, AlertSeverity severity, String message) {
        ProcessAlert alert = new ProcessAlert();
        alert.setShipment(shipmentRepository.getReferenceById(shipmentId));
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setDetectedAt(Instant.now());
        return processAlertRepository.save(alert);
    }

    /**
     * FR-44: Lists alerts matching filters, filtered by user stage view permissions.
     */
    @Transactional(readOnly = true)
    public List<ProcessAlertResponse> list(
            String status,
            AlertSeverity severity,
            UUID batchId,
            UUID shipmentId,
            List<String> roleCodes
    ) {
        Set<String> allowedStages = stagePermissionService.getAllowedStages(roleCodes, StageAction.VIEW);
        if (allowedStages.isEmpty()) {
            return List.of();
        }

        String statusFilter = "OPEN";
        if (status != null) {
            String s = status.trim().toUpperCase();
            if ("RESOLVED".equals(s) || "ALL".equals(s) || "OPEN".equals(s)) {
                statusFilter = s;
            }
        }

        List<ProcessAlert> alerts = processAlertRepository.findWithDetailsFiltered(batchId, shipmentId, severity, statusFilter);

        return alerts.stream()
                .filter(a -> {
                    if (a.getBatch() != null && a.getBatch().getProcessStage() != null) {
                        return allowedStages.contains(a.getBatch().getProcessStage().getCode());
                    }
                    if (a.getShipment() != null) {
                        return allowedStages.contains(ProcessStageCodes.LOGISTICS);
                    }
                    return true;
                })
                .map(this::toResponse)
                .toList();
    }

    /**
     * FR-44 / RB-203: Resolves a process alert with pessimistic locking and stage UPDATE permission check.
     */
    @Transactional
    public ProcessAlertResponse resolve(UUID alertId, UUID resolvedByUserId, List<String> roleCodes) {
        ProcessAlert alert = resolveInternal(alertId, resolvedByUserId, roleCodes);
        return toResponse(alert);
    }

    /**
     * Legacy internal resolve method.
     */
    @Transactional
    public void resolve(UUID alertId, UUID resolvedByUserId) {
        resolveInternal(alertId, resolvedByUserId, null);
    }

    private ProcessAlert resolveInternal(UUID alertId, UUID resolvedByUserId, List<String> roleCodes) {
        ProcessAlert alert = processAlertRepository.findByIdForUpdate(alertId)
                .orElseThrow(() -> NotFoundException.of("ProcessAlert", alertId));

        if (alert.getResolvedAt() != null) {
            throw new BusinessRuleViolationException("RB-203", "Process alert is already resolved");
        }

        if (roleCodes != null) {
            if (alert.getBatch() != null && alert.getBatch().getProcessStage() != null) {
                stagePermissionService.assertAllowed(roleCodes, alert.getBatch().getProcessStage().getCode(), StageAction.UPDATE);
            } else if (alert.getShipment() != null) {
                stagePermissionService.assertAllowed(roleCodes, ProcessStageCodes.LOGISTICS, StageAction.UPDATE);
            } else {
                Set<String> allowed = stagePermissionService.getAllowedStages(roleCodes, StageAction.UPDATE);
                if (allowed.isEmpty()) {
                    throw new ForbiddenStageActionException("ALERT", StageAction.UPDATE.name());
                }
            }
        }

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("id", alert.getId().toString());
        before.put("alertType", alert.getAlertType());
        before.put("severity", alert.getSeverity() != null ? alert.getSeverity().name() : null);
        before.put("message", alert.getMessage());
        before.put("batchId", alert.getBatch() != null ? alert.getBatch().getId().toString() : null);
        before.put("shipmentId", alert.getShipment() != null ? alert.getShipment().getId().toString() : null);
        before.put("detectedAt", alert.getDetectedAt() != null ? alert.getDetectedAt().toString() : null);
        before.put("resolvedAt", null);
        before.put("resolvedBy", null);

        Instant now = Instant.now();
        alert.setResolvedAt(now);
        AppUser resolver = null;
        if (resolvedByUserId != null) {
            resolver = appUserRepository.findById(resolvedByUserId).orElse(null);
        }
        alert.setResolvedBy(resolver);
        ProcessAlert saved = processAlertRepository.save(alert);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("id", saved.getId().toString());
        after.put("alertType", saved.getAlertType());
        after.put("severity", saved.getSeverity() != null ? saved.getSeverity().name() : null);
        after.put("message", saved.getMessage());
        after.put("batchId", saved.getBatch() != null ? saved.getBatch().getId().toString() : null);
        after.put("shipmentId", saved.getShipment() != null ? saved.getShipment().getId().toString() : null);
        after.put("detectedAt", saved.getDetectedAt() != null ? saved.getDetectedAt().toString() : null);
        after.put("resolvedAt", saved.getResolvedAt() != null ? saved.getResolvedAt().toString() : null);
        after.put("resolvedBy", resolver != null ? resolver.getUsername() : null);

        auditLogService.record(resolvedByUserId, "RESOLVE_ALERT", "process_alert", saved.getId(), toJson(before), toJson(after));

        return saved;
    }

    public ProcessAlertResponse toResponse(ProcessAlert alert) {
        if (alert == null) {
            return null;
        }
        Batch b = alert.getBatch();
        Shipment s = alert.getShipment();
        AppUser r = alert.getResolvedBy();
        return new ProcessAlertResponse(
                alert.getId(),
                b != null ? b.getId() : null,
                b != null ? b.getTraceabilityCode() : null,
                b != null && b.getProcessStage() != null ? b.getProcessStage().getCode() : null,
                s != null ? s.getId() : null,
                s != null ? s.getShipmentNumber() : null,
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getDetectedAt(),
                alert.getResolvedAt(),
                r != null ? r.getId() : null,
                r != null ? r.getUsername() : null
        );
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}
