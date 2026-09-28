package org.dev.tequilacluster.services.bottling;

import org.dev.tequilacluster.dtos.bottling.BottlingBatchCreateRequest;
import org.dev.tequilacluster.dtos.bottling.BottlingBatchResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.bottling.BottlingLabelValue;
import org.dev.tequilacluster.models.bottling.BottlingLabelValueId;
import org.dev.tequilacluster.models.bottling.TaxLabel;
import org.dev.tequilacluster.models.catalogs.Brand;
import org.dev.tequilacluster.models.catalogs.LabelRequirement;
import org.dev.tequilacluster.models.catalogs.TequilaCategory;
import org.dev.tequilacluster.models.distillation.DistillationBatch;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchLineageId;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.models.bottling.enums.BarcodeType;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;
import org.dev.tequilacluster.models.bottling.enums.TaxLabelStatus;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingLabelValueRepository;
import org.dev.tequilacluster.repositories.bottling.TaxLabelRepository;
import org.dev.tequilacluster.repositories.catalogs.BrandRepository;
import org.dev.tequilacluster.repositories.catalogs.LabelRequirementRepository;
import org.dev.tequilacluster.repositories.catalogs.TequilaCategoryRepository;
import org.dev.tequilacluster.repositories.catalogs.ValidationRuleRepository;
import org.dev.tequilacluster.repositories.distillation.DistillationBatchRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchLineageRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessStageRepository;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.dev.tequilacluster.services.shared.TraceabilityCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BottlingBatchService {

    private final BatchRepository batchRepository;
    private final BottlingBatchRepository bottlingBatchRepository;
    private final DistillationBatchRepository distillationBatchRepository;
    private final BatchLineageRepository batchLineageRepository;
    private final TaxLabelRepository taxLabelRepository;
    private final BottledUnitRepository bottledUnitRepository;
    private final BottlingLabelValueRepository bottlingLabelValueRepository;
    private final LabelRequirementRepository labelRequirementRepository;
    private final BrandRepository brandRepository;
    private final TequilaCategoryRepository tequilaCategoryRepository;
    private final ValidationRuleRepository validationRuleRepository;
    private final ProcessStageRepository processStageRepository;
    private final AppUserRepository appUserRepository;
    private final TraceabilityCodeGenerator traceabilityCodeGenerator;
    private final BatchLifecycleService batchLifecycleService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;

    public BottlingBatchService(
            BatchRepository batchRepository,
            BottlingBatchRepository bottlingBatchRepository,
            DistillationBatchRepository distillationBatchRepository,
            BatchLineageRepository batchLineageRepository,
            TaxLabelRepository taxLabelRepository,
            BottledUnitRepository bottledUnitRepository,
            BottlingLabelValueRepository bottlingLabelValueRepository,
            LabelRequirementRepository labelRequirementRepository,
            BrandRepository brandRepository,
            TequilaCategoryRepository tequilaCategoryRepository,
            ValidationRuleRepository validationRuleRepository,
            ProcessStageRepository processStageRepository,
            AppUserRepository appUserRepository,
            TraceabilityCodeGenerator traceabilityCodeGenerator,
            BatchLifecycleService batchLifecycleService,
            AlertService alertService,
            AuditLogService auditLogService) {
        this.batchRepository = batchRepository;
        this.bottlingBatchRepository = bottlingBatchRepository;
        this.distillationBatchRepository = distillationBatchRepository;
        this.batchLineageRepository = batchLineageRepository;
        this.taxLabelRepository = taxLabelRepository;
        this.bottledUnitRepository = bottledUnitRepository;
        this.bottlingLabelValueRepository = bottlingLabelValueRepository;
        this.labelRequirementRepository = labelRequirementRepository;
        this.brandRepository = brandRepository;
        this.tequilaCategoryRepository = tequilaCategoryRepository;
        this.validationRuleRepository = validationRuleRepository;
        this.processStageRepository = processStageRepository;
        this.appUserRepository = appUserRepository;
        this.traceabilityCodeGenerator = traceabilityCodeGenerator;
        this.batchLifecycleService = batchLifecycleService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public BottlingBatchResponse create(BottlingBatchCreateRequest request, UUID currentUserId) {
        // 1. FR-20 / RB-301
        DistillationBatch distillationBatch = distillationBatchRepository.findById(request.distillationBatchId())
                .orElseThrow(() -> NotFoundException.of("DistillationBatch", request.distillationBatchId()));
        Batch parentBatch = distillationBatch.getBatch();
        if (parentBatch.getStatus() != BatchStatus.COMPLETED) {
            throw new BusinessRuleViolationException("RB-301", "Source distillation batch is not COMPLETED");
        }

        // 2. FR-20 / RB-205
        if (distillationBatch.getReadyForBottlingAt() == null || distillationBatch.getReadyForBottlingAt().isAfter(Instant.now())) {
            throw new BusinessRuleViolationException("RB-205", "Maturation period not yet complete");
        }

        // 3. FR-20 / RB-206
        TequilaCategory category = tequilaCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> NotFoundException.of("TequilaCategory", request.categoryId()));
        Integer requiredDays = distillationBatch.getRequiredMaturationDays() != null ? distillationBatch.getRequiredMaturationDays() : 0;
        Integer minDays = category.getMinimumMaturationDays() != null ? category.getMinimumMaturationDays() : 0;
        if (requiredDays < minDays) {
            throw new BusinessRuleViolationException("RB-206", "Required maturation days do not meet category minimum");
        }

        // 4.
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> NotFoundException.of("Brand", request.brandId()));

        // 5. FR-21
        if (bottlingBatchRepository.existsByProductionLotNumber(request.productionLotNumber())) {
            throw new BusinessRuleViolationException("RB-301", "Production lot number already exists");
        }

        // 6. FR-22 / RB-302
        if (request.assignedTaxLabelIds().size() < request.unitsBottled()) {
            throw new BusinessRuleViolationException("RB-302", "Not enough tax labels assigned");
        }

        // 7. FR-22 / RB-303
        List<TaxLabel> taxLabels = taxLabelRepository.findAllById(request.assignedTaxLabelIds());
        if (taxLabels.size() != request.assignedTaxLabelIds().size()) {
            throw new BusinessRuleViolationException("RB-303", "One or more tax labels not found");
        }
        for (TaxLabel label : taxLabels) {
            if (label.getStatus() != TaxLabelStatus.AVAILABLE) {
                throw new BusinessRuleViolationException("RB-303", "Tax label is not available: " + label.getFolio());
            }
        }

        // 8. FR-24 / RB-305
        List<LabelRequirement> requiredLabels = labelRequirementRepository.findByRequiredTrueAndActiveTrue();
        for (LabelRequirement requirement : requiredLabels) {
            if (!request.labelValues().containsKey(requirement.getId())) {
                throw new BusinessRuleViolationException("RB-305", "Missing label value for requirement: " + requirement.getCode());
            }
        }

        // 9.
        ProcessStage stage = processStageRepository.findById(ProcessStageCodes.BOTTLING)
                .orElseThrow(() -> NotFoundException.of("ProcessStage", ProcessStageCodes.BOTTLING));
        Batch batch = new Batch();
        batch.setTraceabilityCode(traceabilityCodeGenerator.next());
        batch.setProcessStage(stage);
        batch.setStatus(BatchStatus.DRAFT);
        batch.setCreatedAt(Instant.now());
        if (currentUserId != null) batch.setCreatedBy(appUserRepository.getReferenceById(currentUserId));
        batch = batchRepository.save(batch);
        final UUID batchId = batch.getId();

        // 10.
        BottlingBatch bb = new BottlingBatch();
        bb.setBatch(batch);
        bb.setBrand(brand);
        bb.setCategory(category);
        bb.setBottlingDate(request.bottlingDate());
        bb.setBottleCapacityMl(request.bottleCapacityMl());
        bb.setTotalVolumeL(request.totalVolumeL());
        bb.setProductionLotNumber(request.productionLotNumber());
        bb.setUnitsBottled(request.unitsBottled());
        bb.setRegisteredLossesUnits(request.registeredLossesUnits() != null ? request.registeredLossesUnits() : 0);
        bb.setNotes(request.notes());
        bb = bottlingBatchRepository.save(bb);

        // 11.
        BatchLineage lineage = new BatchLineage();
        lineage.setId(new BatchLineageId(request.distillationBatchId(), batchId));
        lineage.setParentBatch(parentBatch);
        lineage.setChildBatch(batch);
        if (currentUserId != null) lineage.setLinkedBy(appUserRepository.getReferenceById(currentUserId));
        lineage.setLinkedAt(Instant.now());
        batchLineageRepository.save(lineage);

        // 12. FR-22 + FR-23
        for (int i = 0; i < request.unitsBottled(); i++) {
            TaxLabel label = taxLabels.get(i);
            label.setStatus(TaxLabelStatus.ASSIGNED);
            label.setBottlingBatch(bb);
            label.setAssignedAt(Instant.now());
            taxLabelRepository.save(label);

            BottledUnit unit = new BottledUnit();
            unit.setBottlingBatch(bb);
            unit.setUnitCode(batch.getTraceabilityCode() + "-U" + String.format("%05d", i + 1));
            unit.setBarcodeType(BarcodeType.QR);
            unit.setTaxLabel(label);
            unit.setStatus(BottledUnitStatus.AVAILABLE);
            unit.setCreatedAt(Instant.now());
            bottledUnitRepository.save(unit);

            label.setStatus(TaxLabelStatus.USED);
            label.setUsedAt(Instant.now());
            taxLabelRepository.save(label);
        }

        // 13. FR-24
        for (var entry : request.labelValues().entrySet()) {
            LabelRequirement req = labelRequirementRepository.findById(entry.getKey())
                    .orElseThrow(() -> NotFoundException.of("LabelRequirement", entry.getKey()));
            BottlingLabelValue blv = new BottlingLabelValue();
            blv.setId(new BottlingLabelValueId(batchId, entry.getKey()));
            blv.setBottlingBatch(bb);
            blv.setRequirement(req);
            blv.setValueText(entry.getValue());
            bottlingLabelValueRepository.save(blv);
        }

        // 14. FR-25 / RB-306
        boolean reconciliationWarning = false;
        int expected = request.unitsBottled() + (request.registeredLossesUnits() != null ? request.registeredLossesUnits() : 0);
        int actual = request.assignedTaxLabelIds().size();
        if (expected > 0) {
            BigDecimal discrepancy = BigDecimal.valueOf(Math.abs(actual - expected))
                    .divide(BigDecimal.valueOf(expected), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            var ruleOpt = validationRuleRepository.findByProcessStageCodeAndParameterCodeAndActiveTrue("BOTTLING", "TAX_LABEL_RECONCILIATION");
            if (ruleOpt.isPresent()) {
                var rule = ruleOpt.get();
                if (rule.getAllowedDeviation() != null && discrepancy.compareTo(rule.getAllowedDeviation()) > 0) {
                    alertService.raiseForBatch(batchId, "TAX_LABEL_RECONCILIATION", AlertSeverity.WARNING,
                            String.format("Tax label reconciliation discrepancy: %.2f%% (allowed: %.2f%%)", discrepancy, rule.getAllowedDeviation()));
                    reconciliationWarning = true;
                }
            }
        }

        // 15.
        batchLifecycleService.start(batchId, currentUserId);
        auditLogService.record(currentUserId, "CREATE", "bottling_batch", batchId, null, null);

        // 16.
        return getResponse(bb, reconciliationWarning);
    }

    @Transactional(readOnly = true)
    public BottlingBatchResponse get(UUID batchId) {
        BottlingBatch bb = bottlingBatchRepository.findById(batchId)
                .orElseThrow(() -> NotFoundException.of("BottlingBatch", batchId));
        return getResponse(bb, false);
    }

    @Transactional
    public void complete(UUID batchId, UUID currentUserId) {
        BottlingBatch bb = bottlingBatchRepository.findById(batchId)
                .orElseThrow(() -> NotFoundException.of("BottlingBatch", batchId));
        
        List<LabelRequirement> requiredLabels = labelRequirementRepository.findByRequiredTrueAndActiveTrue();
        for (LabelRequirement req : requiredLabels) {
            boolean exists = bottlingLabelValueRepository.existsById(new BottlingLabelValueId(batchId, req.getId()));
            if (!exists) {
                throw new BusinessRuleViolationException("RB-307", "Missing label requirement: " + req.getCode());
            }
        }

        batchLifecycleService.complete(batchId, currentUserId);
    }

    private BottlingBatchResponse getResponse(BottlingBatch bb, boolean reconciliationWarning) {
        Batch batch = bb.getBatch();
        int usedLabels = (int) taxLabelRepository.countByBottlingBatchIdAndStatus(batch.getId(), TaxLabelStatus.USED.name());
        int availableUnits = (int) bottledUnitRepository.countByBottlingBatchIdAndStatus(batch.getId(), BottledUnitStatus.AVAILABLE.name());

        String sourceDistCode = null;
        var lineageOpt = batchLineageRepository.findByChildBatchId(batch.getId()).stream().findFirst();
        if (lineageOpt.isPresent()) {
            sourceDistCode = lineageOpt.get().getParentBatch().getTraceabilityCode();
        }

        return new BottlingBatchResponse(
                batch.getId(),
                batch.getTraceabilityCode(),
                batch.getStatus(),
                bb.getBrand() != null ? bb.getBrand().getName() : null,
                bb.getCategory() != null ? bb.getCategory().getName() : null,
                bb.getBottlingDate(),
                bb.getBottleCapacityMl(),
                bb.getTotalVolumeL(),
                bb.getProductionLotNumber(),
                bb.getUnitsBottled(),
                bb.getRegisteredLossesUnits(),
                usedLabels,
                availableUnits,
                sourceDistCode,
                bb.getNotes(),
                reconciliationWarning
        );
    }
}
