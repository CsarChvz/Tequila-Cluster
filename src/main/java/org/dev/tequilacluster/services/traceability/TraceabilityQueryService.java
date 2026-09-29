package org.dev.tequilacluster.services.traceability;

import org.dev.tequilacluster.dtos.alerts.ProcessAlertResponse;
import org.dev.tequilacluster.dtos.quality.NonConformityResponse;
import org.dev.tequilacluster.dtos.traceability.BackwardTraceabilityResponse;
import org.dev.tequilacluster.dtos.traceability.BatchHistoryResponse;
import org.dev.tequilacluster.dtos.traceability.BatchLineageLinkDto;
import org.dev.tequilacluster.dtos.traceability.BatchTransitionHistoryDto;
import org.dev.tequilacluster.dtos.traceability.BottledUnitDetailDto;
import org.dev.tequilacluster.dtos.traceability.BottlingBatchDetailDto;
import org.dev.tequilacluster.dtos.traceability.DistillationBatchDetailDto;
import org.dev.tequilacluster.dtos.traceability.ForwardTraceabilityResponse;
import org.dev.tequilacluster.dtos.traceability.HarvestOriginDto;
import org.dev.tequilacluster.dtos.traceability.ImpactedBottlingBatchDto;
import org.dev.tequilacluster.dtos.traceability.ImpactedShipmentDto;
import org.dev.tequilacluster.dtos.traceability.JimaBatchDetailDto;
import org.dev.tequilacluster.dtos.traceability.TraceabilityNodeDto;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.distillation.DistillationBatch;
import org.dev.tequilacluster.models.harvest.JimaBatch;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.logistics.ShipmentItem;
import org.dev.tequilacluster.models.quality.NonConformity;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchTransitionHistory;
import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.catalogs.AgaveFieldRepository;
import org.dev.tequilacluster.repositories.catalogs.SupplierRepository;
import org.dev.tequilacluster.repositories.distillation.DistillationBatchRepository;
import org.dev.tequilacluster.repositories.harvest.JimaBatchRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentItemRepository;
import org.dev.tequilacluster.repositories.quality.NonConformityRepository;
import org.dev.tequilacluster.repositories.shared.BatchLineageRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.BatchTransitionHistoryRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for backward traceability (FR-37), forward traceability (FR-38),
 * and batch lifecycle history (FR-43).
 */
@Service
public class TraceabilityQueryService {

    private final BatchRepository batchRepository;
    private final BatchLineageRepository batchLineageRepository;
    private final BottledUnitRepository bottledUnitRepository;
    private final JimaBatchRepository jimaBatchRepository;
    private final DistillationBatchRepository distillationBatchRepository;
    private final BottlingBatchRepository bottlingBatchRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final BatchTransitionHistoryRepository batchTransitionHistoryRepository;
    private final ProcessAlertRepository processAlertRepository;
    private final NonConformityRepository nonConformityRepository;
    private final SupplierRepository supplierRepository;
    private final AgaveFieldRepository agaveFieldRepository;
    private final StagePermissionService stagePermissionService;
    private final AlertService alertService;

    public TraceabilityQueryService(
            BatchRepository batchRepository,
            BatchLineageRepository batchLineageRepository,
            BottledUnitRepository bottledUnitRepository,
            JimaBatchRepository jimaBatchRepository,
            DistillationBatchRepository distillationBatchRepository,
            BottlingBatchRepository bottlingBatchRepository,
            ShipmentItemRepository shipmentItemRepository,
            BatchTransitionHistoryRepository batchTransitionHistoryRepository,
            ProcessAlertRepository processAlertRepository,
            NonConformityRepository nonConformityRepository,
            SupplierRepository supplierRepository,
            AgaveFieldRepository agaveFieldRepository,
            StagePermissionService stagePermissionService,
            AlertService alertService
    ) {
        this.batchRepository = batchRepository;
        this.batchLineageRepository = batchLineageRepository;
        this.bottledUnitRepository = bottledUnitRepository;
        this.jimaBatchRepository = jimaBatchRepository;
        this.distillationBatchRepository = distillationBatchRepository;
        this.bottlingBatchRepository = bottlingBatchRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.batchTransitionHistoryRepository = batchTransitionHistoryRepository;
        this.processAlertRepository = processAlertRepository;
        this.nonConformityRepository = nonConformityRepository;
        this.supplierRepository = supplierRepository;
        this.agaveFieldRepository = agaveFieldRepository;
        this.stagePermissionService = stagePermissionService;
        this.alertService = alertService;
    }

    /**
     * FR-37: Backward traceability from BottledUnit.unitCode or Batch.traceabilityCode
     * walks backwards through BatchLineage up to Harvest/Jima batches and their agricultural origins.
     */
    @Transactional(readOnly = true)
    public BackwardTraceabilityResponse backward(String code, List<String> roleCodes) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessRuleViolationException("RB-501", "Traceability code or unit code cannot be blank");
        }
        String trimmedCode = code.trim();

        String searchType;
        Batch targetBatch;
        BottledUnitDetailDto bottledUnitDetail = null;

        Optional<BottledUnit> unitOpt = bottledUnitRepository.findByUnitCodeWithBatch(trimmedCode);
        if (unitOpt.isPresent()) {
            searchType = "BOTTLED_UNIT";
            BottledUnit unit = unitOpt.get();
            BottlingBatch bb = unit.getBottlingBatch();
            targetBatch = bb.getBatch();
            bottledUnitDetail = new BottledUnitDetailDto(
                    unit.getId(),
                    unit.getUnitCode(),
                    unit.getBarcodeType(),
                    unit.getStatus(),
                    unit.getTaxLabel() != null ? unit.getTaxLabel().getId() : null,
                    unit.getTaxLabel() != null ? unit.getTaxLabel().getFolio() : null,
                    bb.getId(),
                    bb.getProductionLotNumber()
            );
        } else {
            Optional<Batch> batchOpt = batchRepository.findByTraceabilityCodeWithStage(trimmedCode);
            if (batchOpt.isEmpty()) {
                throw NotFoundException.of("Batch or BottledUnit", trimmedCode);
            }
            searchType = "BATCH";
            targetBatch = batchOpt.get();
        }

        stagePermissionService.assertAllowed(roleCodes, targetBatch.getProcessStage().getCode(), StageAction.VIEW);

        // Backward BFS traversal using batch queries
        Set<UUID> visited = new HashSet<>();
        visited.add(targetBatch.getId());

        Map<UUID, Batch> batchesById = new LinkedHashMap<>();
        batchesById.put(targetBatch.getId(), targetBatch);

        List<BatchLineageLinkDto> links = new ArrayList<>();
        Set<String> linkKeys = new HashSet<>();
        Set<UUID> harvestBatchIds = new HashSet<>();

        if (ProcessStageCodes.HARVEST.equalsIgnoreCase(targetBatch.getProcessStage().getCode())) {
            harvestBatchIds.add(targetBatch.getId());
        }

        List<UUID> frontier = List.of(targetBatch.getId());
        while (!frontier.isEmpty()) {
            List<BatchLineage> lineages = batchLineageRepository.findByChildBatch_IdIn(frontier);
            List<UUID> newFrontier = new ArrayList<>();
            for (BatchLineage lineage : lineages) {
                Batch parent = lineage.getParentBatch();
                Batch child = lineage.getChildBatch();

                String linkKey = parent.getId() + "->" + child.getId();
                if (linkKeys.add(linkKey)) {
                    links.add(new BatchLineageLinkDto(
                            parent.getId(),
                            child.getId(),
                            lineage.getQuantityUsed(),
                            lineage.getUnit(),
                            lineage.getLinkedAt()
                    ));
                }

                if (visited.add(parent.getId())) {
                    batchesById.put(parent.getId(), parent);
                    newFrontier.add(parent.getId());
                    if (ProcessStageCodes.HARVEST.equalsIgnoreCase(parent.getProcessStage().getCode())) {
                        harvestBatchIds.add(parent.getId());
                    }
                }
            }
            frontier = newFrontier;
        }

        // Fetch harvest origins eagerly in 1 query
        List<HarvestOriginDto> harvestOrigins = new ArrayList<>();
        if (!harvestBatchIds.isEmpty()) {
            List<JimaBatch> jimaBatches = jimaBatchRepository.findWithDetailsByBatchIdIn(harvestBatchIds);
            for (JimaBatch jb : jimaBatches) {
                harvestOrigins.add(new HarvestOriginDto(
                        jb.getBatchId(),
                        jb.getBatch() != null ? jb.getBatch().getTraceabilityCode() : null,
                        jb.getHarvestDate(),
                        jb.getTotalWeightKg(),
                        jb.getAgaveHeartsCount(),
                        jb.getEstimatedYieldL(),
                        jb.getEstimatedYieldFactor(),
                        jb.getField().getId(),
                        jb.getField().getFieldCode(),
                        jb.getField().getName(),
                        jb.getSupplier().getId(),
                        jb.getSupplier().getLegalName(),
                        jb.getSupplier().getTaxId(),
                        jb.getField().getAuthorizedArea().getId(),
                        jb.getField().getAuthorizedArea().getCode(),
                        jb.getField().getAuthorizedArea().getName(),
                        jb.getField().getAuthorizedArea().getStateName(),
                        jb.getField().getAuthorizedArea().getMunicipality()
                ));
            }
        }

        Map<UUID, BigDecimal> volumes = loadBatchVolumes(batchesById.values());
        List<TraceabilityNodeDto> nodes = batchesById.values().stream()
                .map(b -> toNodeDto(b, volumes.get(b.getId())))
                .toList();

        TraceabilityNodeDto targetBatchNode = toNodeDto(targetBatch, volumes.get(targetBatch.getId()));

        return new BackwardTraceabilityResponse(
                trimmedCode,
                searchType,
                targetBatchNode,
                bottledUnitDetail,
                nodes,
                links,
                harvestOrigins
        );
    }

    /**
     * FR-38: Forward traceability from a Batch, Supplier, or AgaveField
     * walks forward through BatchLineage descendants down to bottling batches and shipments.
     */
    @Transactional(readOnly = true)
    public ForwardTraceabilityResponse forward(UUID batchId, UUID supplierId, UUID fieldId, List<String> roleCodes) {
        int count = (batchId != null ? 1 : 0) + (supplierId != null ? 1 : 0) + (fieldId != null ? 1 : 0);
        if (count != 1) {
            throw new BusinessRuleViolationException("RB-502",
                    "Exactly one origin parameter must be provided: batchId, supplierId, or fieldId");
        }

        String originType;
        UUID originId;
        List<Batch> rootBatches;

        if (batchId != null) {
            originType = "BATCH";
            originId = batchId;
            Batch batch = batchRepository.findByIdWithStage(batchId)
                    .orElseThrow(() -> NotFoundException.of("Batch", batchId));
            stagePermissionService.assertAllowed(roleCodes, batch.getProcessStage().getCode(), StageAction.VIEW);
            rootBatches = List.of(batch);
        } else if (supplierId != null) {
            originType = "SUPPLIER";
            originId = supplierId;
            if (!supplierRepository.existsById(supplierId)) {
                throw NotFoundException.of("Supplier", supplierId);
            }
            stagePermissionService.assertAllowed(roleCodes, ProcessStageCodes.HARVEST, StageAction.VIEW);
            List<JimaBatch> jimaBatches = jimaBatchRepository.findBySupplier_Id(supplierId);
            List<UUID> jimaBatchIds = jimaBatches.stream().map(JimaBatch::getBatchId).toList();
            rootBatches = jimaBatchIds.isEmpty() ? List.of() : batchRepository.findAllByIdInWithStage(jimaBatchIds);
        } else {
            originType = "FIELD";
            originId = fieldId;
            if (!agaveFieldRepository.existsById(fieldId)) {
                throw NotFoundException.of("AgaveField", fieldId);
            }
            stagePermissionService.assertAllowed(roleCodes, ProcessStageCodes.HARVEST, StageAction.VIEW);
            List<JimaBatch> jimaBatches = jimaBatchRepository.findByField_Id(fieldId);
            List<UUID> jimaBatchIds = jimaBatches.stream().map(JimaBatch::getBatchId).toList();
            rootBatches = jimaBatchIds.isEmpty() ? List.of() : batchRepository.findAllByIdInWithStage(jimaBatchIds);
        }

        // Forward BFS traversal
        Set<UUID> visited = new HashSet<>();
        Map<UUID, Batch> batchesById = new LinkedHashMap<>();
        List<UUID> frontier = new ArrayList<>();
        Set<UUID> bottlingBatchIds = new HashSet<>();

        for (Batch b : rootBatches) {
            visited.add(b.getId());
            batchesById.put(b.getId(), b);
            frontier.add(b.getId());
            if (ProcessStageCodes.BOTTLING.equalsIgnoreCase(b.getProcessStage().getCode())) {
                bottlingBatchIds.add(b.getId());
            }
        }

        List<BatchLineageLinkDto> links = new ArrayList<>();
        Set<String> linkKeys = new HashSet<>();

        while (!frontier.isEmpty()) {
            List<BatchLineage> lineages = batchLineageRepository.findByParentBatch_IdIn(frontier);
            List<UUID> newFrontier = new ArrayList<>();
            for (BatchLineage lineage : lineages) {
                Batch parent = lineage.getParentBatch();
                Batch child = lineage.getChildBatch();

                String linkKey = parent.getId() + "->" + child.getId();
                if (linkKeys.add(linkKey)) {
                    links.add(new BatchLineageLinkDto(
                            parent.getId(),
                            child.getId(),
                            lineage.getQuantityUsed(),
                            lineage.getUnit(),
                            lineage.getLinkedAt()
                    ));
                }

                if (visited.add(child.getId())) {
                    batchesById.put(child.getId(), child);
                    newFrontier.add(child.getId());
                    if (ProcessStageCodes.BOTTLING.equalsIgnoreCase(child.getProcessStage().getCode())) {
                        bottlingBatchIds.add(child.getId());
                    }
                }
            }
            frontier = newFrontier;
        }

        Map<UUID, BigDecimal> volumes = loadBatchVolumes(batchesById.values());
        List<TraceabilityNodeDto> nodes = batchesById.values().stream()
                .map(b -> toNodeDto(b, volumes.get(b.getId())))
                .toList();

        List<ImpactedBottlingBatchDto> impactedBottlingBatches = new ArrayList<>();
        List<ImpactedShipmentDto> impactedShipments = new ArrayList<>();

        if (!bottlingBatchIds.isEmpty()) {
            List<BottlingBatch> bottlingBatches = bottlingBatchRepository.findWithDetailsByIdIn(bottlingBatchIds);

            List<Object[]> statusCounts = bottledUnitRepository.countGroupByStatusForBatchIds(bottlingBatchIds);
            Map<UUID, Map<String, Long>> statusCountsByBatch = new HashMap<>();
            for (Object[] row : statusCounts) {
                UUID bId = (UUID) row[0];
                String st = row[1] != null ? row[1].toString() : "UNKNOWN";
                Long cnt = ((Number) row[2]).longValue();
                statusCountsByBatch.computeIfAbsent(bId, k -> new LinkedHashMap<>()).put(st, cnt);
            }

            for (BottlingBatch bb : bottlingBatches) {
                impactedBottlingBatches.add(new ImpactedBottlingBatchDto(
                        bb.getId(),
                        bb.getBatch() != null ? bb.getBatch().getTraceabilityCode() : null,
                        bb.getProductionLotNumber(),
                        bb.getBrand() != null ? bb.getBrand().getName() : null,
                        bb.getCategory() != null ? bb.getCategory().getName() : null,
                        bb.getBottlingDate(),
                        bb.getBottleCapacityMl(),
                        bb.getTotalVolumeL(),
                        bb.getUnitsBottled(),
                        statusCountsByBatch.getOrDefault(bb.getId(), Map.of())
                ));
            }

            List<ShipmentItem> shipmentItems = shipmentItemRepository.findWithShipmentByBottlingBatchIdIn(bottlingBatchIds);
            for (ShipmentItem si : shipmentItems) {
                Shipment s = si.getShipment();
                impactedShipments.add(new ImpactedShipmentDto(
                        s.getId(),
                        s.getShipmentNumber(),
                        s.getStatus(),
                        s.getCarrier() != null ? s.getCarrier().getName() : null,
                        s.getDestination(),
                        s.getDepartureAt(),
                        s.getDeliveredAt(),
                        si.getBottlingBatch().getId(),
                        si.getQuantityUnits()
                ));
            }
        }

        return new ForwardTraceabilityResponse(
                originType,
                originId,
                nodes,
                links,
                impactedBottlingBatches,
                impactedShipments
        );
    }

    /**
     * FR-43: Full history and stage details of a batch from its traceabilityCode.
     */
    @Transactional(readOnly = true)
    public BatchHistoryResponse getHistory(String traceabilityCode, List<String> roleCodes) {
        if (traceabilityCode == null || traceabilityCode.trim().isEmpty()) {
            throw new BusinessRuleViolationException("RB-501", "Traceability code cannot be blank");
        }
        String trimmedCode = traceabilityCode.trim();

        Batch batch = batchRepository.findByTraceabilityCodeWithStage(trimmedCode)
                .orElseThrow(() -> NotFoundException.of("Batch", trimmedCode));

        stagePermissionService.assertAllowed(roleCodes, batch.getProcessStage().getCode(), StageAction.VIEW);

        Object stageDetail = null;
        String stageCode = batch.getProcessStage().getCode();
        if (ProcessStageCodes.HARVEST.equalsIgnoreCase(stageCode)) {
            stageDetail = jimaBatchRepository.findWithDetailsByBatchId(batch.getId())
                    .map(jb -> new JimaBatchDetailDto(
                            jb.getField().getId(),
                            jb.getField().getFieldCode(),
                            jb.getField().getName(),
                            jb.getSupplier().getId(),
                            jb.getSupplier().getLegalName(),
                            jb.getSupplier().getTaxId(),
                            jb.getField().getAuthorizedArea().getId(),
                            jb.getField().getAuthorizedArea().getCode(),
                            jb.getField().getAuthorizedArea().getName(),
                            jb.getHarvestDate(),
                            jb.getTotalWeightKg(),
                            jb.getAgaveHeartsCount(),
                            jb.getEstimatedYieldL(),
                            jb.getEstimatedYieldFactor(),
                            jb.getNotes()
                    ))
                    .orElse(null);
        } else if (ProcessStageCodes.DISTILLATION.equalsIgnoreCase(stageCode)) {
            stageDetail = distillationBatchRepository.findById(batch.getId())
                    .map(db -> new DistillationBatchDetailDto(
                            db.getDistillationDate(),
                            db.getTotalDistilledVolumeL(),
                            db.getHeadsVolumeL(),
                            db.getHeartsVolumeL(),
                            db.getTailsVolumeL(),
                            db.getAlcoholContentPct(),
                            db.getCookingTemperatureC(),
                            db.getFermentationPh(),
                            db.getActualYieldL(),
                            db.getEstimatedYieldLSnapshot(),
                            db.getMaturationRequired(),
                            db.getMaturationStartDate(),
                            db.getRequiredMaturationDays(),
                            db.getReadyForBottlingAt(),
                            db.getNotes()
                    ))
                    .orElse(null);
        } else if (ProcessStageCodes.BOTTLING.equalsIgnoreCase(stageCode)) {
            stageDetail = bottlingBatchRepository.findWithDetailsById(batch.getId())
                    .map(bb -> new BottlingBatchDetailDto(
                            bb.getBrand() != null ? bb.getBrand().getId() : null,
                            bb.getBrand() != null ? bb.getBrand().getName() : null,
                            bb.getCategory() != null ? bb.getCategory().getId() : null,
                            bb.getCategory() != null ? bb.getCategory().getName() : null,
                            bb.getBottlingDate(),
                            bb.getBottleCapacityMl(),
                            bb.getTotalVolumeL(),
                            bb.getProductionLotNumber(),
                            bb.getUnitsBottled(),
                            bb.getRegisteredLossesUnits(),
                            bb.getNotes()
                    ))
                    .orElse(null);
        }

        List<BatchTransitionHistory> transitions = batchTransitionHistoryRepository
                .findByBatchIdOrderByChangedAtDescWithDetails(batch.getId());
        List<BatchTransitionHistoryDto> transitionDtos = transitions.stream()
                .map(t -> new BatchTransitionHistoryDto(
                        t.getId(),
                        t.getFromStage() != null ? t.getFromStage().getCode() : null,
                        t.getFromStage() != null ? t.getFromStage().getName() : null,
                        t.getToStage() != null ? t.getToStage().getCode() : null,
                        t.getToStage() != null ? t.getToStage().getName() : null,
                        t.getFromStatus(),
                        t.getToStatus(),
                        t.getChangedBy() != null ? t.getChangedBy().getId() : null,
                        t.getChangedBy() != null ? t.getChangedBy().getUsername() : null,
                        t.getReason(),
                        t.getChangedAt()
                ))
                .toList();

        List<ProcessAlert> alerts = processAlertRepository
                .findByBatchIdOrderByDetectedAtDescWithDetails(batch.getId());
        List<ProcessAlertResponse> alertDtos = alerts.stream()
                .map(alertService::toResponse)
                .toList();

        List<NonConformity> nonConformities = nonConformityRepository.findByBatch_Id(batch.getId());
        List<NonConformityResponse> nonConformityDtos = nonConformities.stream()
                .map(nc -> new NonConformityResponse(
                        nc.getId(),
                        nc.getBatch() != null ? nc.getBatch().getId() : null,
                        nc.getBatch() != null ? nc.getBatch().getTraceabilityCode() : null,
                        nc.getBatch() != null && nc.getBatch().getProcessStage() != null ? nc.getBatch().getProcessStage().getCode() : null,
                        nc.getBottledUnit() != null ? nc.getBottledUnit().getId() : null,
                        nc.getBottledUnit() != null ? nc.getBottledUnit().getUnitCode() : null,
                        nc.getTitle(),
                        nc.getDescription(),
                        nc.getSeverity(),
                        nc.getStatus(),
                        nc.getReportedBy() != null ? nc.getReportedBy().getId() : null,
                        nc.getReportedBy() != null ? nc.getReportedBy().getUsername() : null,
                        nc.getReportedAt(),
                        nc.getResolvedAt()
                ))
                .toList();

        AppUser createdBy = batch.getCreatedBy();

        return new BatchHistoryResponse(
                batch.getId(),
                batch.getTraceabilityCode(),
                batch.getProcessStage().getCode(),
                batch.getProcessStage().getName(),
                batch.getStatus(),
                createdBy != null ? createdBy.getId() : null,
                createdBy != null ? createdBy.getUsername() : null,
                batch.getCreatedAt(),
                batch.getCompletedAt(),
                batch.getCancelledAt(),
                batch.getCancellationReason(),
                stageDetail,
                transitionDtos,
                alertDtos,
                nonConformityDtos
        );
    }

    private TraceabilityNodeDto toNodeDto(Batch b, BigDecimal volume) {
        return new TraceabilityNodeDto(
                b.getId(),
                b.getTraceabilityCode(),
                b.getProcessStage().getCode(),
                b.getProcessStage().getName(),
                b.getStatus(),
                volume,
                b.getCreatedAt(),
                b.getCompletedAt(),
                b.getCancelledAt()
        );
    }

    private Map<UUID, BigDecimal> loadBatchVolumes(Collection<Batch> batches) {
        Map<UUID, BigDecimal> volumes = new HashMap<>();
        Set<UUID> harvestIds = new HashSet<>();
        Set<UUID> distIds = new HashSet<>();
        Set<UUID> botIds = new HashSet<>();

        for (Batch b : batches) {
            String stage = b.getProcessStage().getCode();
            if (ProcessStageCodes.HARVEST.equalsIgnoreCase(stage)) {
                harvestIds.add(b.getId());
            } else if (ProcessStageCodes.DISTILLATION.equalsIgnoreCase(stage)) {
                distIds.add(b.getId());
            } else if (ProcessStageCodes.BOTTLING.equalsIgnoreCase(stage)) {
                botIds.add(b.getId());
            }
        }

        if (!harvestIds.isEmpty()) {
            List<JimaBatch> jimaList = jimaBatchRepository.findAllById(harvestIds);
            for (JimaBatch jb : jimaList) {
                volumes.put(jb.getBatchId(), jb.getEstimatedYieldL() != null ? jb.getEstimatedYieldL() : jb.getTotalWeightKg());
            }
        }
        if (!distIds.isEmpty()) {
            List<DistillationBatch> distList = distillationBatchRepository.findAllById(distIds);
            for (DistillationBatch db : distList) {
                volumes.put(db.getBatchId(), db.getActualYieldL() != null ? db.getActualYieldL() : db.getTotalDistilledVolumeL());
            }
        }
        if (!botIds.isEmpty()) {
            List<BottlingBatch> botList = bottlingBatchRepository.findAllById(botIds);
            for (BottlingBatch bb : botList) {
                volumes.put(bb.getId(), bb.getTotalVolumeL());
            }
        }

        return volumes;
    }
}
