package org.dev.tequilacluster.services.quality;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.quality.NonConformityCreateRequest;
import org.dev.tequilacluster.dtos.quality.NonConformityResponse;
import org.dev.tequilacluster.dtos.quality.NonConformityStatusUpdateRequest;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.quality.NonConformity;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.quality.NonConformityRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Quality stage service: FR-33, RB-407 (Non-conformities).
 */
@Service
public class NonConformityService {

    private static final Logger log = LoggerFactory.getLogger(NonConformityService.class);

    private final NonConformityRepository nonConformityRepository;
    private final BatchRepository batchRepository;
    private final BottledUnitRepository bottledUnitRepository;
    private final AppUserRepository appUserRepository;
    private final StagePermissionService stagePermissionService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public NonConformityService(
            NonConformityRepository nonConformityRepository,
            BatchRepository batchRepository,
            BottledUnitRepository bottledUnitRepository,
            AppUserRepository appUserRepository,
            StagePermissionService stagePermissionService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.nonConformityRepository = nonConformityRepository;
        this.batchRepository = batchRepository;
        this.bottledUnitRepository = bottledUnitRepository;
        this.appUserRepository = appUserRepository;
        this.stagePermissionService = stagePermissionService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * FR-33: Registra una nueva no conformidad sobre un lote y opcionalmente sobre una unidad embotellada.
     */
    @Transactional
    public NonConformityResponse create(NonConformityCreateRequest request, UUID currentUserId, List<String> roleCodes) {
        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> NotFoundException.of("Batch", request.batchId()));

        stagePermissionService.assertAllowed(roleCodes, batch.getProcessStage().getCode(), StageAction.CREATE);

        BottledUnit bottledUnit = null;
        if (request.bottledUnitId() != null) {
            bottledUnit = bottledUnitRepository.findById(request.bottledUnitId())
                    .orElseThrow(() -> NotFoundException.of("BottledUnit", request.bottledUnitId()));

            if (bottledUnit.getBottlingBatch() == null
                    || !batch.getId().equals(bottledUnit.getBottlingBatch().getId())) {
                log.warn("RB-407: rejected non-conformity — bottled unit {} does not belong to batch {}", request.bottledUnitId(), request.batchId());
                throw new BusinessRuleViolationException("RB-407",
                        "Bottled unit " + request.bottledUnitId() + " does not belong to the specified batch " + request.batchId());
            }
        }

        NonConformity nonConformity = new NonConformity();
        nonConformity.setBatch(batch);
        nonConformity.setBottledUnit(bottledUnit);
        nonConformity.setTitle(request.title().trim());
        nonConformity.setDescription(request.description().trim());
        nonConformity.setSeverity(request.severity());
        nonConformity.setStatus(NonConformityStatus.OPEN);
        nonConformity.setReportedAt(Instant.now());
        if (currentUserId != null) {
            appUserRepository.findById(currentUserId).ifPresent(nonConformity::setReportedBy);
        }

        nonConformity = nonConformityRepository.save(nonConformity);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("batchId", batch.getId().toString());
        after.put("bottledUnitId", bottledUnit != null ? bottledUnit.getId().toString() : null);
        after.put("severity", nonConformity.getSeverity().name());
        after.put("status", nonConformity.getStatus().name());
        after.put("title", nonConformity.getTitle());
        auditLogService.record(currentUserId, "CREATE", "non_conformity", nonConformity.getId(), null, toJson(after));

        if (nonConformity.getSeverity() == NonConformitySeverity.CRITICAL) {
            log.error("CRITICAL non-conformity {} reported on batch {}: {}", nonConformity.getId(), batch.getId(), nonConformity.getTitle());
        } else {
            log.info("Non-conformity {} ({}) reported on batch {}: {}", nonConformity.getId(), nonConformity.getSeverity(), batch.getId(), nonConformity.getTitle());
        }
        return toResponse(nonConformity);
    }

    /**
     * FR-33: Consulta una no conformidad por su ID.
     */
    @Transactional(readOnly = true)
    public NonConformityResponse getById(UUID id, List<String> roleCodes) {
        NonConformity nonConformity = nonConformityRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("NonConformity", id));

        stagePermissionService.assertAllowed(roleCodes, nonConformity.getBatch().getProcessStage().getCode(), StageAction.VIEW);

        return toResponse(nonConformity);
    }

    /**
     * FR-33: Lista no conformidades con filtros opcionales (batchId, status, severity),
     * acotadas a las etapas en las que el usuario posee permiso de VIEW.
     */
    @Transactional(readOnly = true)
    public List<NonConformityResponse> list(
            UUID batchId,
            NonConformityStatus status,
            NonConformitySeverity severity,
            List<String> roleCodes
    ) {
        Set<String> allowedStages = stagePermissionService.getAllowedStages(roleCodes, StageAction.VIEW);
        if (allowedStages.isEmpty()) {
            return List.of();
        }

        List<NonConformity> candidates;
        if (batchId != null) {
            candidates = nonConformityRepository.findByBatch_Id(batchId);
        } else if (status != null) {
            candidates = nonConformityRepository.findByStatus(status);
        } else if (severity != null) {
            candidates = nonConformityRepository.findBySeverity(severity);
        } else {
            candidates = nonConformityRepository.findAll();
        }

        return candidates.stream()
                .filter(nc -> nc.getBatch() != null && nc.getBatch().getProcessStage() != null
                        && allowedStages.contains(nc.getBatch().getProcessStage().getCode()))
                .filter(nc -> batchId == null || (nc.getBatch() != null && batchId.equals(nc.getBatch().getId())))
                .filter(nc -> status == null || nc.getStatus() == status)
                .filter(nc -> severity == null || nc.getSeverity() == severity)
                .map(this::toResponse)
                .toList();
    }

    /**
     * FR-33 / RB-407: Actualiza el estado de una no conformidad siguiendo estrictamente:
     * OPEN → INVESTIGATING → RESOLVED → CLOSED.
     * Carga bajo bloqueo pesimista (PESSIMISTIC_WRITE) para evitar carreras de transición.
     */
    @Transactional
    public NonConformityResponse updateStatus(
            UUID id,
            NonConformityStatusUpdateRequest request,
            UUID currentUserId,
            List<String> roleCodes
    ) {
        NonConformity nonConformity = nonConformityRepository.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.of("NonConformity", id));

        stagePermissionService.assertAllowed(roleCodes, nonConformity.getBatch().getProcessStage().getCode(), StageAction.UPDATE);

        NonConformityStatus current = nonConformity.getStatus();
        NonConformityStatus target = request.status();

        if (current == target) {
            log.warn("RB-407: rejected no-op status update for non-conformity {} (already {})", id, current);
            throw new BusinessRuleViolationException("RB-407",
                    "Non-conformity is already in status: " + current);
        }

        boolean validTransition = switch (current) {
            case OPEN -> target == NonConformityStatus.INVESTIGATING;
            case INVESTIGATING -> target == NonConformityStatus.RESOLVED;
            case RESOLVED -> target == NonConformityStatus.CLOSED;
            case CLOSED -> false;
        };

        if (!validTransition) {
            log.warn("RB-407: rejected invalid non-conformity status transition {} -> {} for {}", current, target, id);
            throw new BusinessRuleViolationException("RB-407",
                    "Invalid status transition from " + current + " to " + target);
        }

        Instant previousResolvedAt = nonConformity.getResolvedAt();
        nonConformity.setStatus(target);
        if (target == NonConformityStatus.RESOLVED) {
            nonConformity.setResolvedAt(Instant.now());
        }
        // Al pasar a CLOSED se conserva el resolvedAt existente; no se sobreescribe.

        nonConformity = nonConformityRepository.save(nonConformity);

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("status", current.name());
        before.put("resolvedAt", previousResolvedAt != null ? previousResolvedAt.toString() : null);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", target.name());
        after.put("resolvedAt", nonConformity.getResolvedAt() != null ? nonConformity.getResolvedAt().toString() : null);

        auditLogService.record(currentUserId, "UPDATE_STATUS", "non_conformity", nonConformity.getId(), toJson(before), toJson(after));
        log.info("Non-conformity {} status changed {} -> {} by user {}", id, current, target, currentUserId);

        return toResponse(nonConformity);
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

    private NonConformityResponse toResponse(NonConformity nc) {
        Batch batch = nc.getBatch();
        BottledUnit unit = nc.getBottledUnit();
        AppUser reporter = nc.getReportedBy();

        return new NonConformityResponse(
                nc.getId(),
                batch != null ? batch.getId() : null,
                batch != null ? batch.getTraceabilityCode() : null,
                batch != null && batch.getProcessStage() != null ? batch.getProcessStage().getCode() : null,
                unit != null ? unit.getId() : null,
                unit != null ? unit.getUnitCode() : null,
                nc.getTitle(),
                nc.getDescription(),
                nc.getSeverity(),
                nc.getStatus(),
                reporter != null ? reporter.getId() : null,
                reporter != null ? reporter.getUsername() : null,
                nc.getReportedAt(),
                nc.getResolvedAt()
        );
    }
}
