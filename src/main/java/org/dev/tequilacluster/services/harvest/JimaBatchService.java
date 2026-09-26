package org.dev.tequilacluster.services.harvest;

import org.dev.tequilacluster.dtos.harvest.JimaBatchCreateRequest;
import org.dev.tequilacluster.dtos.harvest.JimaBatchResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.AgaveField;
import org.dev.tequilacluster.models.catalogs.AuthorizedProductionArea;
import org.dev.tequilacluster.models.catalogs.Supplier;
import org.dev.tequilacluster.models.harvest.JimaBatch;
import org.dev.tequilacluster.models.harvest.TransportPermit;
import org.dev.tequilacluster.models.harvest.enums.TransportPermitStatus;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.catalogs.AgaveFieldRepository;
import org.dev.tequilacluster.repositories.catalogs.SupplierRepository;
import org.dev.tequilacluster.repositories.harvest.JimaBatchRepository;
import org.dev.tequilacluster.repositories.harvest.TransportPermitRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessStageRepository;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.services.shared.TraceabilityCodeGenerator;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Harvest (Jima) stage, end to end: FR-05 to FR-11. This is the reference implementation for
 * the other stages (distillation/bottling/logistics) — replicate this shape (validate → create
 * Batch + detail → side effects → start()) rather than reinventing the flow.
 */
@Service
public class JimaBatchService {

    private static final BigDecimal DEFAULT_YIELD_FACTOR = new BigDecimal("0.12");

    private final BatchRepository batchRepository;
    private final JimaBatchRepository jimaBatchRepository;
    private final TransportPermitRepository transportPermitRepository;
    private final AgaveFieldRepository agaveFieldRepository;
    private final SupplierRepository supplierRepository;
    private final ProcessStageRepository processStageRepository;
    private final AppUserRepository appUserRepository;
    private final TraceabilityCodeGenerator traceabilityCodeGenerator;
    private final BatchLifecycleService batchLifecycleService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final BigDecimal plantMaxCapacityKg;

    public JimaBatchService(
            BatchRepository batchRepository,
            JimaBatchRepository jimaBatchRepository,
            TransportPermitRepository transportPermitRepository,
            AgaveFieldRepository agaveFieldRepository,
            SupplierRepository supplierRepository,
            ProcessStageRepository processStageRepository,
            AppUserRepository appUserRepository,
            TraceabilityCodeGenerator traceabilityCodeGenerator,
            BatchLifecycleService batchLifecycleService,
            AlertService alertService,
            AuditLogService auditLogService,
            @Value("${process.plant-max-capacity-kg}") BigDecimal plantMaxCapacityKg
    ) {
        this.batchRepository = batchRepository;
        this.jimaBatchRepository = jimaBatchRepository;
        this.transportPermitRepository = transportPermitRepository;
        this.agaveFieldRepository = agaveFieldRepository;
        this.supplierRepository = supplierRepository;
        this.processStageRepository = processStageRepository;
        this.appUserRepository = appUserRepository;
        this.traceabilityCodeGenerator = traceabilityCodeGenerator;
        this.batchLifecycleService = batchLifecycleService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
        this.plantMaxCapacityKg = plantMaxCapacityKg;
    }

    @Transactional
    public JimaBatchResponse create(JimaBatchCreateRequest request, UUID currentUserId) {
        AgaveField field = agaveFieldRepository.findById(request.fieldId())
                .orElseThrow(() -> NotFoundException.of("AgaveField", request.fieldId()));

        // RB-101/FR-07: the field's authorized production area must be active and within its
        // validity dates. Fail fast, before creating anything.
        assertAuthorizedAreaIsValid(field.getAuthorizedArea());

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> NotFoundException.of("Supplier", request.supplierId()));

        Batch batch = createDraftBatch(currentUserId);

        // RB-102/FR-08: block if the supplier is inactive, but keep the DRAFT batch (with an
        // open CRITICAL alert) instead of silently discarding the attempt — an Auditor/
        // Administrator can still see it happened.
        if (!Boolean.TRUE.equals(supplier.getActive())) {
            alertService.raiseForBatch(batch.getId(), "SUPPLIER_INACTIVE", AlertSeverity.CRITICAL,
                    "Supplier %s is inactive or not registered".formatted(request.supplierId()));
            throw new BusinessRuleViolationException("RB-102",
                    "Supplier is not registered or inactive: " + request.supplierId());
        }

        // RB-103/FR-09
        BigDecimal yieldFactor = DEFAULT_YIELD_FACTOR;
        BigDecimal estimatedYieldL = request.totalWeightKg().multiply(yieldFactor);

        JimaBatch jimaBatch = new JimaBatch();
        jimaBatch.setBatch(batch);
        jimaBatch.setField(field);
        jimaBatch.setSupplier(supplier);
        jimaBatch.setHarvestDate(request.harvestDate());
        jimaBatch.setTotalWeightKg(request.totalWeightKg());
        jimaBatch.setAgaveHeartsCount(request.agaveHeartsCount());
        jimaBatch.setEstimatedYieldL(estimatedYieldL);
        jimaBatch.setEstimatedYieldFactor(yieldFactor);
        jimaBatch.setNotes(request.notes());
        jimaBatchRepository.save(jimaBatch);

        // RB-106/FR-10: transport permit is always auto-generated with the harvest batch.
        TransportPermit permit = new TransportPermit();
        permit.setJimaBatch(jimaBatch);
        permit.setPermitNumber(generatePermitNumber(batch.getTraceabilityCode()));
        permit.setStatus(TransportPermitStatus.GENERATED);
        permit.setGeneratedAt(Instant.now());
        transportPermitRepository.save(permit);

        // RB-104/FR-11: non-blocking warning above the configured plant capacity.
        boolean capacityWarning = request.totalWeightKg().compareTo(plantMaxCapacityKg) > 0;
        if (capacityWarning) {
            alertService.raiseForBatch(batch.getId(), "PLANT_CAPACITY_EXCEEDED", AlertSeverity.WARNING,
                    "Harvest weight %s kg exceeds plant capacity %s kg".formatted(request.totalWeightKg(), plantMaxCapacityKg));
        }

        batchLifecycleService.start(batch.getId(), currentUserId);
        auditLogService.record(currentUserId, "CREATE", "jima_batch", batch.getId(), null, null);

        return toResponse(batch, jimaBatch, permit, capacityWarning);
    }

    @Transactional(readOnly = true)
    public JimaBatchResponse get(UUID batchId) {
        JimaBatch jimaBatch = jimaBatchRepository.findById(batchId)
                .orElseThrow(() -> NotFoundException.of("JimaBatch", batchId));
        TransportPermit permit = transportPermitRepository.findByJimaBatch_BatchId(batchId).orElse(null);
        boolean capacityWarning = plantMaxCapacityKg != null
                && jimaBatch.getTotalWeightKg().compareTo(plantMaxCapacityKg) > 0;
        return toResponse(jimaBatch.getBatch(), jimaBatch, permit, capacityWarning);
    }

    private void assertAuthorizedAreaIsValid(AuthorizedProductionArea area) {
        LocalDate today = LocalDate.now();
        boolean active = Boolean.TRUE.equals(area.getActive());
        boolean afterStart = area.getValidFrom() == null || !today.isBefore(area.getValidFrom());
        boolean beforeEnd = area.getValidTo() == null || !today.isAfter(area.getValidTo());
        if (!active || !afterStart || !beforeEnd) {
            throw new BusinessRuleViolationException("RB-101",
                    "Agave field belongs to an inactive or expired authorized production area: " + area.getCode());
        }
    }

    private Batch createDraftBatch(UUID currentUserId) {
        ProcessStage harvestStage = processStageRepository.findById(ProcessStageCodes.HARVEST)
                .orElseThrow(() -> NotFoundException.of("ProcessStage", ProcessStageCodes.HARVEST));

        Batch batch = new Batch();
        batch.setTraceabilityCode(traceabilityCodeGenerator.next());
        batch.setProcessStage(harvestStage);
        batch.setStatus(BatchStatus.DRAFT);
        batch.setCreatedAt(Instant.now());
        if (currentUserId != null) {
            AppUser userRef = appUserRepository.getReferenceById(currentUserId);
            batch.setCreatedBy(userRef);
        }
        return batchRepository.save(batch);
    }

    private String generatePermitNumber(String traceabilityCode) {
        return "PERMIT-" + traceabilityCode;
    }

    private JimaBatchResponse toResponse(Batch batch, JimaBatch jimaBatch, TransportPermit permit, boolean capacityWarning) {
        return new JimaBatchResponse(
                batch.getId(),
                batch.getTraceabilityCode(),
                batch.getStatus(),
                jimaBatch.getField().getFieldCode(),
                jimaBatch.getSupplier().getSupplierCode(),
                jimaBatch.getHarvestDate(),
                jimaBatch.getTotalWeightKg(),
                jimaBatch.getAgaveHeartsCount(),
                jimaBatch.getEstimatedYieldL(),
                jimaBatch.getEstimatedYieldFactor(),
                permit == null ? null : permit.getPermitNumber(),
                capacityWarning
        );
    }
}
