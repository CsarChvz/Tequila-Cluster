package org.dev.tequilacluster.services.distillation;

import org.dev.tequilacluster.dtos.distillation.DistillationBatchCreateRequest;
import org.dev.tequilacluster.dtos.distillation.DistillationBatchResponse;
import org.dev.tequilacluster.models.distillation.DistillationBatch;
import org.dev.tequilacluster.models.harvest.JimaBatch;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchLineageId;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.distillation.DistillationBatchRepository;
import org.dev.tequilacluster.repositories.harvest.JimaBatchRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchLineageRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessStageRepository;
import org.dev.tequilacluster.repositories.catalogs.ValidationRuleRepository;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.services.shared.TraceabilityCodeGenerator;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DistillationBatchService {

    private final BatchRepository batchRepository;
    private final DistillationBatchRepository distillationBatchRepository;
    private final JimaBatchRepository jimaBatchRepository;
    private final BatchLineageRepository batchLineageRepository;
    private final ValidationRuleRepository validationRuleRepository;
    private final ProcessStageRepository processStageRepository;
    private final AppUserRepository appUserRepository;
    private final TraceabilityCodeGenerator traceabilityCodeGenerator;
    private final BatchLifecycleService batchLifecycleService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;

    public DistillationBatchService(
            BatchRepository batchRepository,
            DistillationBatchRepository distillationBatchRepository,
            JimaBatchRepository jimaBatchRepository,
            BatchLineageRepository batchLineageRepository,
            ValidationRuleRepository validationRuleRepository,
            ProcessStageRepository processStageRepository,
            AppUserRepository appUserRepository,
            TraceabilityCodeGenerator traceabilityCodeGenerator,
            BatchLifecycleService batchLifecycleService,
            AlertService alertService,
            AuditLogService auditLogService
    ) {
        this.batchRepository = batchRepository;
        this.distillationBatchRepository = distillationBatchRepository;
        this.jimaBatchRepository = jimaBatchRepository;
        this.batchLineageRepository = batchLineageRepository;
        this.validationRuleRepository = validationRuleRepository;
        this.processStageRepository = processStageRepository;
        this.appUserRepository = appUserRepository;
        this.traceabilityCodeGenerator = traceabilityCodeGenerator;
        this.batchLifecycleService = batchLifecycleService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DistillationBatchResponse create(DistillationBatchCreateRequest request, UUID currentUserId) {
        // FR-12 / RB-201
        for (var item : request.sourceHarvestBatches()) {
            Batch batch = batchRepository.findById(item.harvestBatchId())
                    .orElseThrow(() -> NotFoundException.of("Batch", item.harvestBatchId()));
            if (batch.getStatus() != BatchStatus.COMPLETED || !batch.getProcessStage().getCode().equals(ProcessStageCodes.HARVEST)) {
                throw new BusinessRuleViolationException("RB-201", "Source harvest batch is not COMPLETED: " + item.harvestBatchId());
            }
        }

        // FR-15 / RB-202
        BigDecimal sumCuts = request.headsVolumeL().add(request.heartsVolumeL()).add(request.tailsVolumeL());
        if (sumCuts.compareTo(request.totalDistilledVolumeL()) > 0) {
            throw new BusinessRuleViolationException("RB-202", "Sum of cuts exceeds total distilled volume");
        }

        // FR-19 / RB-205
        if (request.maturationRequired()) {
            if (request.maturationStartDate() == null || request.requiredMaturationDays() == null || request.requiredMaturationDays() <= 0) {
                throw new BusinessRuleViolationException("RB-205", "Maturation requires start date and days > 0");
            }
        }

        ProcessStage stage = processStageRepository.findById(ProcessStageCodes.DISTILLATION)
                .orElseThrow(() -> NotFoundException.of("ProcessStage", ProcessStageCodes.DISTILLATION));

        Batch batch = new Batch();
        batch.setTraceabilityCode(traceabilityCodeGenerator.next());
        batch.setProcessStage(stage);
        batch.setStatus(BatchStatus.DRAFT);
        batch.setCreatedAt(Instant.now());
        if (currentUserId != null) {
            batch.setCreatedBy(appUserRepository.getReferenceById(currentUserId));
        }
        batch = batchRepository.save(batch);
        final UUID batchId = batch.getId();

        BigDecimal estimatedYieldLSnapshot = BigDecimal.ZERO;
        for (var item : request.sourceHarvestBatches()) {
            JimaBatch jimaBatch = jimaBatchRepository.findById(item.harvestBatchId())
                    .orElseThrow(() -> NotFoundException.of("JimaBatch", item.harvestBatchId()));
            estimatedYieldLSnapshot = estimatedYieldLSnapshot.add(jimaBatch.getEstimatedYieldL());
        }

        Instant readyForBottlingAt;
        if (request.maturationRequired()) {
            readyForBottlingAt = request.maturationStartDate()
                    .plusDays(request.requiredMaturationDays())
                    .atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else {
            readyForBottlingAt = Instant.now();
        }

        DistillationBatch db = new DistillationBatch();
        db.setBatch(batch);
        db.setDistillationDate(request.distillationDate());
        db.setTotalDistilledVolumeL(request.totalDistilledVolumeL());
        db.setHeadsVolumeL(request.headsVolumeL());
        db.setHeartsVolumeL(request.heartsVolumeL());
        db.setTailsVolumeL(request.tailsVolumeL());
        db.setAlcoholContentPct(request.alcoholContentPct());
        db.setCookingTemperatureC(request.cookingTemperatureC());
        db.setFermentationPh(request.fermentationPh());
        db.setActualYieldL(request.actualYieldL());
        db.setEstimatedYieldLSnapshot(estimatedYieldLSnapshot);
        db.setMaturationRequired(request.maturationRequired());
        db.setMaturationStartDate(request.maturationStartDate());
        db.setRequiredMaturationDays(request.requiredMaturationDays() != null ? request.requiredMaturationDays() : 0);
        db.setReadyForBottlingAt(readyForBottlingAt);
        db.setNotes(request.notes());
        db = distillationBatchRepository.save(db);

        // Lineage
        for (var item : request.sourceHarvestBatches()) {
            Batch parentBatch = batchRepository.getReferenceById(item.harvestBatchId());
            BatchLineage lineage = new BatchLineage();
            lineage.setId(new BatchLineageId(item.harvestBatchId(), batchId));
            lineage.setParentBatch(parentBatch);
            lineage.setChildBatch(batch);
            lineage.setQuantityUsed(item.quantityUsed());
            lineage.setUnit(item.unit());
            if (currentUserId != null) {
                lineage.setLinkedBy(appUserRepository.getReferenceById(currentUserId));
            }
            lineage.setLinkedAt(Instant.now());
            batchLineageRepository.save(lineage);
        }

        // Alerts
        checkRange("DISTILLATION", "ALCOHOL_CONTENT_PCT", request.alcoholContentPct(), batchId, AlertSeverity.CRITICAL, "Alcohol content %.3f%% is outside the permitted range");

        if (estimatedYieldLSnapshot.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal deviation = request.actualYieldL().subtract(estimatedYieldLSnapshot).abs()
                    .divide(estimatedYieldLSnapshot, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            validationRuleRepository.findByProcessStageCodeAndParameterCodeAndActiveTrue("DISTILLATION", "YIELD_DEVIATION")
                    .ifPresent(rule -> {
                        if (rule.getAllowedDeviation() != null && deviation.compareTo(rule.getAllowedDeviation()) > 0) {
                            alertService.raiseForBatch(batchId, "YIELD_DEVIATION", AlertSeverity.WARNING,
                                    "Actual yield deviates %.2f%% from estimated (allowed: %.2f%%)".formatted(deviation, rule.getAllowedDeviation()));
                        }
                    });
        }

        if (request.cookingTemperatureC() != null) {
            checkRange("DISTILLATION", "COOKING_TEMPERATURE_C", request.cookingTemperatureC(), batchId, AlertSeverity.WARNING, "Cooking temperature %.3f°C is outside the permitted range");
        }
        if (request.fermentationPh() != null) {
            checkRange("DISTILLATION", "FERMENTATION_PH", request.fermentationPh(), batchId, AlertSeverity.WARNING, "Fermentation pH %.3f is outside the permitted range");
        }

        batchLifecycleService.start(batchId, currentUserId);
        auditLogService.record(currentUserId, "CREATE", "distillation_batch", batchId, null, null);

        List<String> parentCodes = getSourceCodes(batchId);
        return toResponse(batch, db, parentCodes);
    }

    @Transactional(readOnly = true)
    public DistillationBatchResponse get(UUID batchId) {
        DistillationBatch db = distillationBatchRepository.findById(batchId)
                .orElseThrow(() -> NotFoundException.of("DistillationBatch", batchId));
        return toResponse(db.getBatch(), db, getSourceCodes(batchId));
    }

    @Transactional
    public void complete(UUID batchId, UUID currentUserId) {
        batchLifecycleService.complete(batchId, currentUserId);
    }

    /** Backs the distillation dashboard table — every distillation batch, newest first. */
    @Transactional(readOnly = true)
    public List<DistillationBatchResponse> list() {
        return distillationBatchRepository.findAll().stream()
                .sorted((a, b) -> b.getBatch().getCreatedAt().compareTo(a.getBatch().getCreatedAt()))
                .map(db -> toResponse(db.getBatch(), db, getSourceCodes(db.getBatch().getId())))
                .toList();
    }

    private void checkRange(String stageCode, String paramCode, BigDecimal value, UUID batchId, AlertSeverity severity, String msgTemplate) {
        validationRuleRepository.findByProcessStageCodeAndParameterCodeAndActiveTrue(stageCode, paramCode)
                .ifPresent(rule -> {
                    boolean belowMin = rule.getMinValue() != null && value.compareTo(rule.getMinValue()) < 0;
                    boolean aboveMax = rule.getMaxValue() != null && value.compareTo(rule.getMaxValue()) > 0;
                    if (belowMin || aboveMax) {
                        alertService.raiseForBatch(batchId, paramCode, severity, msgTemplate.formatted(value));
                    }
                });
    }

    private List<String> getSourceCodes(UUID childBatchId) {
        return batchLineageRepository.findByChildBatchId(childBatchId).stream()
                .map(lineage -> lineage.getParentBatch().getTraceabilityCode())
                .collect(Collectors.toList());
    }

    private DistillationBatchResponse toResponse(Batch batch, DistillationBatch db, List<String> sourceCodes) {
        return new DistillationBatchResponse(
                batch.getId(), batch.getTraceabilityCode(), batch.getStatus(),
                db.getDistillationDate(), db.getTotalDistilledVolumeL(),
                db.getHeadsVolumeL(), db.getHeartsVolumeL(), db.getTailsVolumeL(),
                db.getAlcoholContentPct(), db.getCookingTemperatureC(), db.getFermentationPh(),
                db.getActualYieldL(), db.getEstimatedYieldLSnapshot(),
                db.getMaturationRequired(), db.getMaturationStartDate(),
                db.getRequiredMaturationDays(), db.getReadyForBottlingAt(),
                db.getNotes(), sourceCodes
        );
    }
}
