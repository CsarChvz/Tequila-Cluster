package org.dev.tequilacluster.services.quality;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.quality.RecallCreateRequest;
import org.dev.tequilacluster.dtos.quality.RecallResponse;
import org.dev.tequilacluster.dtos.quality.RecallStatusUpdateRequest;
import org.dev.tequilacluster.dtos.quality.RecallUnitResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;
import org.dev.tequilacluster.models.logistics.ShipmentUnit;
import org.dev.tequilacluster.models.quality.NonConformity;
import org.dev.tequilacluster.models.quality.Recall;
import org.dev.tequilacluster.models.quality.RecallUnit;
import org.dev.tequilacluster.models.quality.RecallUnitId;
import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import org.dev.tequilacluster.models.quality.enums.RecallType;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentUnitRepository;
import org.dev.tequilacluster.repositories.quality.NonConformityRepository;
import org.dev.tequilacluster.repositories.quality.RecallRepository;
import org.dev.tequilacluster.repositories.quality.RecallUnitRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchLineageRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Quality stage service: FR-34, RB-407 (Recall of products).
 */
@Service
public class RecallService {

    private final RecallRepository recallRepository;
    private final RecallUnitRepository recallUnitRepository;
    private final BatchRepository batchRepository;
    private final BottlingBatchRepository bottlingBatchRepository;
    private final BottledUnitRepository bottledUnitRepository;
    private final BatchLineageRepository batchLineageRepository;
    private final NonConformityRepository nonConformityRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentUnitRepository shipmentUnitRepository;
    private final AppUserRepository appUserRepository;
    private final StagePermissionService stagePermissionService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public RecallService(
            RecallRepository recallRepository,
            RecallUnitRepository recallUnitRepository,
            BatchRepository batchRepository,
            BottlingBatchRepository bottlingBatchRepository,
            BottledUnitRepository bottledUnitRepository,
            BatchLineageRepository batchLineageRepository,
            NonConformityRepository nonConformityRepository,
            ShipmentRepository shipmentRepository,
            ShipmentUnitRepository shipmentUnitRepository,
            AppUserRepository appUserRepository,
            StagePermissionService stagePermissionService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.recallRepository = recallRepository;
        this.recallUnitRepository = recallUnitRepository;
        this.batchRepository = batchRepository;
        this.bottlingBatchRepository = bottlingBatchRepository;
        this.bottledUnitRepository = bottledUnitRepository;
        this.batchLineageRepository = batchLineageRepository;
        this.nonConformityRepository = nonConformityRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentUnitRepository = shipmentUnitRepository;
        this.appUserRepository = appUserRepository;
        this.stagePermissionService = stagePermissionService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * FR-34 / RB-407: Registra un nuevo retiro de producto (PARTIAL o COMPLETE).
     */
    @Transactional
    public RecallResponse create(RecallCreateRequest request, UUID currentUserId, List<String> roleCodes) {
        Batch sourceBatch = batchRepository.findById(request.sourceBatchId())
                .orElseThrow(() -> NotFoundException.of("Batch", request.sourceBatchId()));

        stagePermissionService.assertAllowed(roleCodes, sourceBatch.getProcessStage().getCode(), StageAction.CREATE);

        NonConformity nonConformity = null;
        if (request.nonConformityId() != null) {
            nonConformity = nonConformityRepository.findById(request.nonConformityId())
                    .orElseThrow(() -> NotFoundException.of("NonConformity", request.nonConformityId()));

            if (nonConformity.getBatch() == null || !sourceBatch.getId().equals(nonConformity.getBatch().getId())) {
                throw new BusinessRuleViolationException("RB-407",
                        "Non-conformity " + request.nonConformityId() + " does not belong to the source batch " + sourceBatch.getId());
            }
        }

        Set<UUID> allowedBottlingBatchIds = findDescendantBottlingBatchIds(sourceBatch.getId());

        List<UUID> sortedUnitIds;
        if (request.recallType() == RecallType.PARTIAL) {
            if (request.bottledUnitIds() == null || request.bottledUnitIds().isEmpty()) {
                throw new BusinessRuleViolationException("RB-407",
                        "bottledUnitIds must not be empty for a PARTIAL recall");
            }
            Set<UUID> uniqueIds = new HashSet<>(request.bottledUnitIds());
            if (uniqueIds.size() != request.bottledUnitIds().size()) {
                throw new BusinessRuleViolationException("RB-407",
                        "Duplicate bottled unit IDs in recall request");
            }
            sortedUnitIds = uniqueIds.stream().sorted().toList();
        } else {
            // COMPLETE
            if (request.bottledUnitIds() != null && !request.bottledUnitIds().isEmpty()) {
                throw new BusinessRuleViolationException("RB-407",
                        "bottledUnitIds must be null or empty for a COMPLETE recall");
            }
            if (allowedBottlingBatchIds.isEmpty()) {
                throw new BusinessRuleViolationException("RB-407",
                        "No bottling batches found in lineage for source batch " + sourceBatch.getId());
            }
            sortedUnitIds = bottledUnitRepository.findIdsByBottlingBatchIdIn(allowedBottlingBatchIds);
            if (sortedUnitIds.isEmpty()) {
                throw new BusinessRuleViolationException("RB-407",
                        "No bottled units found for source batch " + sourceBatch.getId());
            }
        }

        // ============================================================
        // Concurrencia y Lock Ordering:
        // 1. BottlingBatch locks, UUID ASC
        // 2. Descubrir ShipmentUnit activas
        // 3. Shipment locks, UUID ASC
        // 4. BottledUnit locks, UUID ASC
        // 5. Revalidación final (linaje, estados de botella y re-verificación de ShipmentUnits activas)
        // 6. Modificaciones
        // ============================================================

        // 1. Determinar todos los bottlingBatchId de las botellas afectadas
        List<UUID> affectedBatchIds = bottledUnitRepository.findDistinctBottlingBatchIdsByUnitIds(sortedUnitIds);
        List<UUID> sortedBatchIds = affectedBatchIds.stream()
                .distinct()
                .sorted()
                .toList();

        for (UUID batchId : sortedBatchIds) {
            bottlingBatchRepository.findByIdForUpdate(batchId)
                    .orElseThrow(() -> NotFoundException.of("BottlingBatch", batchId));
        }

        // 2. Descubrir ShipmentUnit activas
        List<ShipmentUnit> activeShipmentUnitsPre = shipmentUnitRepository.findByBottledUnit_IdInAndReleasedAtIsNull(sortedUnitIds);

        // 3. Shipment locks, UUID ASC
        List<UUID> sortedShipmentIds = activeShipmentUnitsPre.stream()
                .map(su -> su.getShipment().getId())
                .distinct()
                .sorted()
                .toList();

        for (UUID shipmentId : sortedShipmentIds) {
            shipmentRepository.findByIdForUpdate(shipmentId)
                    .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));
        }

        // 4. BottledUnit locks, UUID ASC
        List<BottledUnit> bottles = bottledUnitRepository.findAllByIdInForUpdate(sortedUnitIds);
        if (bottles.size() != sortedUnitIds.size()) {
            throw new BusinessRuleViolationException("RB-407",
                    "One or more bottled units could not be found during recall lock acquisition");
        }

        // 5. Revalidación final con todos los locks adquiridos
        for (BottledUnit bottle : bottles) {
            if (bottle.getBottlingBatch() == null || !allowedBottlingBatchIds.contains(bottle.getBottlingBatch().getId())) {
                throw new BusinessRuleViolationException("RB-407",
                        "Bottled unit " + bottle.getUnitCode() + " does not belong to source batch lineage");
            }

            BottledUnitStatus st = bottle.getStatus();
            if (st == BottledUnitStatus.RECALLED || st == BottledUnitStatus.LOST || st == BottledUnitStatus.DAMAGED) {
                throw new BusinessRuleViolationException("RB-407",
                        "Bottled unit " + bottle.getUnitCode() + " is in invalid status for recall: " + st);
            }
        }

        // Revalidar asignaciones ShipmentUnit activas tras adquirir los locks de Shipment y BottledUnit
        List<ShipmentUnit> freshActiveShipmentUnits = shipmentUnitRepository.findByBottledUnit_IdInAndReleasedAtIsNull(sortedUnitIds);
        Map<UUID, ShipmentUnit> freshActiveUnitMap = freshActiveShipmentUnits.stream()
                .collect(Collectors.toMap(su -> su.getBottledUnit().getId(), su -> su));

        // Crear la entidad Recall en estado OPEN
        Recall recall = new Recall();
        recall.setSourceBatch(sourceBatch);
        recall.setNonConformity(nonConformity);
        recall.setRecallType(request.recallType());
        recall.setReason(request.reason().trim());
        recall.setStatus(RecallStatus.OPEN);
        recall.setStartedAt(Instant.now());
        if (currentUserId != null) {
            appUserRepository.findById(currentUserId).ifPresent(recall::setStartedBy);
        }
        recall = recallRepository.save(recall);

        // 6. Modificaciones: procesar botellas y ShipmentUnits según el estado revalidado
        List<ShipmentUnit> unitsToRelease = new ArrayList<>();
        Instant now = Instant.now();

        for (BottledUnit bottle : bottles) {
            if (bottle.getStatus() == BottledUnitStatus.RESERVED) {
                ShipmentUnit su = freshActiveUnitMap.get(bottle.getId());
                if (su != null) {
                    su.setReleasedAt(now);
                    unitsToRelease.add(su);
                }
            }
            // En AVAILABLE, RESERVED, SHIPPED, DELIVERED pasa a RECALLED
            bottle.setStatus(BottledUnitStatus.RECALLED);
        }

        if (!unitsToRelease.isEmpty()) {
            shipmentUnitRepository.saveAll(unitsToRelease);
        }
        bottledUnitRepository.saveAll(bottles);

        // Registrar filas individuales en recall_unit (tanto para PARTIAL como para COMPLETE)
        List<RecallUnit> recallUnits = new ArrayList<>(bottles.size());
        for (BottledUnit bottle : bottles) {
            RecallUnit ru = new RecallUnit();
            ru.setId(new RecallUnitId(recall.getId(), bottle.getId()));
            ru.setRecall(recall);
            ru.setBottledUnit(bottle);
            recallUnits.add(ru);
        }
        recallUnitRepository.saveAll(recallUnits);

        // Auditoría segura con conteo escalar
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("sourceBatchId", sourceBatch.getId().toString());
        after.put("nonConformityId", nonConformity != null ? nonConformity.getId().toString() : null);
        after.put("recallType", recall.getRecallType().name());
        after.put("reason", recall.getReason());
        after.put("status", recall.getStatus().name());
        after.put("affectedUnitsCount", bottles.size());

        auditLogService.record(currentUserId, "CREATE", "recall", recall.getId(), null, toJson(after));

        return toResponse(recall, bottles.size());
    }

    /**
     * FR-34: Consulta un Recall por su ID.
     */
    @Transactional(readOnly = true)
    public RecallResponse getById(UUID id, List<String> roleCodes) {
        Recall recall = recallRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Recall", id));

        stagePermissionService.assertAllowed(roleCodes, recall.getSourceBatch().getProcessStage().getCode(), StageAction.VIEW);

        int count = (int) recallUnitRepository.countByRecall_Id(id);
        return toResponse(recall, count);
    }

    /**
     * FR-34: Lista las botellas físicas individuales afectadas por un Recall.
     */
    @Transactional(readOnly = true)
    public List<RecallUnitResponse> getUnits(UUID id, List<String> roleCodes) {
        Recall recall = recallRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Recall", id));

        stagePermissionService.assertAllowed(roleCodes, recall.getSourceBatch().getProcessStage().getCode(), StageAction.VIEW);

        List<RecallUnit> units = recallUnitRepository.findByRecall_Id(id);
        return units.stream()
                .map(u -> new RecallUnitResponse(
                        u.getRecall().getId(),
                        u.getBottledUnit().getId(),
                        u.getBottledUnit().getUnitCode(),
                        u.getBottledUnit().getStatus()
                ))
                .toList();
    }

    /**
     * FR-34: Lista recalls con filtros opcionales acotados por permisos RBAC.
     */
    @Transactional(readOnly = true)
    public List<RecallResponse> list(
            UUID sourceBatchId,
            RecallStatus status,
            RecallType recallType,
            List<String> roleCodes
    ) {
        Set<String> allowedStages = stagePermissionService.getAllowedStages(roleCodes, StageAction.VIEW);
        if (allowedStages.isEmpty()) {
            return List.of();
        }

        List<Recall> candidates;
        if (sourceBatchId != null) {
            candidates = recallRepository.findBySourceBatch_Id(sourceBatchId);
        } else if (status != null) {
            candidates = recallRepository.findByStatus(status);
        } else {
            candidates = recallRepository.findAll();
        }

        return candidates.stream()
                .filter(r -> r.getSourceBatch() != null && r.getSourceBatch().getProcessStage() != null
                        && allowedStages.contains(r.getSourceBatch().getProcessStage().getCode()))
                .filter(r -> sourceBatchId == null || (r.getSourceBatch() != null && sourceBatchId.equals(r.getSourceBatch().getId())))
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> recallType == null || r.getRecallType() == recallType)
                .map(r -> toResponse(r, (int) recallUnitRepository.countByRecall_Id(r.getId())))
                .toList();
    }

    /**
     * FR-34 / RB-407: Actualiza el estado de un Recall:
     * OPEN → IN_PROGRESS → COMPLETED
     * OPEN → CANCELLED
     * IN_PROGRESS → CANCELLED
     */
    @Transactional
    public RecallResponse updateStatus(
            UUID id,
            RecallStatusUpdateRequest request,
            UUID currentUserId,
            List<String> roleCodes
    ) {
        Recall recall = recallRepository.findByIdForUpdate(id)
                .orElseThrow(() -> NotFoundException.of("Recall", id));

        stagePermissionService.assertAllowed(roleCodes, recall.getSourceBatch().getProcessStage().getCode(), StageAction.UPDATE);

        RecallStatus current = recall.getStatus();
        RecallStatus target = request.status();

        if (current == target) {
            throw new BusinessRuleViolationException("RB-407",
                    "Recall is already in status: " + current);
        }

        boolean validTransition = switch (current) {
            case OPEN -> target == RecallStatus.IN_PROGRESS || target == RecallStatus.CANCELLED;
            case IN_PROGRESS -> target == RecallStatus.COMPLETED || target == RecallStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BusinessRuleViolationException("RB-407",
                    "Invalid status transition from " + current + " to " + target);
        }

        Instant previousCompletedAt = recall.getCompletedAt();

        if (target == RecallStatus.CANCELLED) {
            if (request.reason() == null || request.reason().isBlank()) {
                throw new BusinessRuleViolationException("RB-407",
                        "Cancellation reason is required when cancelling a recall");
            }
            // NO se revierten estados de BottledUnit ni ShipmentUnit.
            recall.setStatus(RecallStatus.CANCELLED);
        } else if (target == RecallStatus.COMPLETED) {
            recall.setStatus(RecallStatus.COMPLETED);
            recall.setCompletedAt(Instant.now());
        } else {
            // IN_PROGRESS
            recall.setStatus(RecallStatus.IN_PROGRESS);
        }

        recall = recallRepository.save(recall);

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("status", current.name());
        before.put("completedAt", previousCompletedAt != null ? previousCompletedAt.toString() : null);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", target.name());
        after.put("completedAt", recall.getCompletedAt() != null ? recall.getCompletedAt().toString() : null);
        if (target == RecallStatus.CANCELLED) {
            after.put("cancellationReason", request.reason().trim());
        }

        auditLogService.record(currentUserId, "UPDATE_STATUS", "recall", recall.getId(), toJson(before), toJson(after));

        int count = (int) recallUnitRepository.countByRecall_Id(id);
        return toResponse(recall, count);
    }

    /**
     * Recorre el grafo de BatchLineage hacia adelante para encontrar todos los BottlingBatches
     * descendientes del lote de origen (o el propio lote si ya es de envasado).
     */
    private Set<UUID> findDescendantBottlingBatchIds(UUID sourceBatchId) {
        Set<UUID> bottlingBatchIds = new HashSet<>();
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new ArrayDeque<>();

        if (bottlingBatchRepository.existsById(sourceBatchId)) {
            bottlingBatchIds.add(sourceBatchId);
        }

        queue.add(sourceBatchId);
        visited.add(sourceBatchId);

        while (!queue.isEmpty()) {
            UUID currentId = queue.poll();
            List<BatchLineage> children = batchLineageRepository.findByParentBatchId(currentId);
            for (BatchLineage bl : children) {
                if (bl.getChildBatch() != null) {
                    UUID childId = bl.getChildBatch().getId();
                    if (visited.add(childId)) {
                        if (bottlingBatchRepository.existsById(childId)) {
                            bottlingBatchIds.add(childId);
                        }
                        queue.add(childId);
                    }
                }
            }
        }

        return bottlingBatchIds;
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

    private RecallResponse toResponse(Recall r, int count) {
        Batch sourceBatch = r.getSourceBatch();
        NonConformity nc = r.getNonConformity();
        AppUser startedBy = r.getStartedBy();

        return new RecallResponse(
                r.getId(),
                sourceBatch != null ? sourceBatch.getId() : null,
                sourceBatch != null ? sourceBatch.getTraceabilityCode() : null,
                sourceBatch != null && sourceBatch.getProcessStage() != null ? sourceBatch.getProcessStage().getCode() : null,
                nc != null ? nc.getId() : null,
                nc != null ? nc.getTitle() : null,
                r.getRecallType(),
                r.getReason(),
                r.getStatus(),
                startedBy != null ? startedBy.getId() : null,
                startedBy != null ? startedBy.getUsername() : null,
                r.getStartedAt(),
                r.getCompletedAt(),
                count
        );
    }
}
