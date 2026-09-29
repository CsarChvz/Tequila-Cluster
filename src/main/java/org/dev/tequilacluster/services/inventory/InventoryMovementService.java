package org.dev.tequilacluster.services.inventory;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.inventory.InventoryMovementCreateRequest;
import org.dev.tequilacluster.dtos.inventory.InventoryMovementResponse;
import org.dev.tequilacluster.dtos.inventory.InventoryReconciliationResponse;
import org.dev.tequilacluster.dtos.inventory.InventoryStockResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;
import org.dev.tequilacluster.models.catalogs.InventoryLocation;
import org.dev.tequilacluster.models.inventory.InventoryMovement;
import org.dev.tequilacluster.models.inventory.enums.MovementType;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.logistics.ShipmentItem;
import org.dev.tequilacluster.models.logistics.ShipmentItemId;
import org.dev.tequilacluster.models.logistics.ShipmentUnit;
import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;
import org.dev.tequilacluster.models.quality.Recall;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.catalogs.InventoryLocationRepository;
import org.dev.tequilacluster.repositories.inventory.BatchLocationStockProjection;
import org.dev.tequilacluster.repositories.inventory.InventoryMovementRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentItemRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentUnitRepository;
import org.dev.tequilacluster.repositories.quality.RecallRepository;
import org.dev.tequilacluster.repositories.quality.RecallUnitRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FR-35, FR-36, RB-506: Servicio de inventario de producto envasado (Kárdex y Reconciliación).
 */
@Service
public class InventoryMovementService {

    private static final Logger log = LoggerFactory.getLogger(InventoryMovementService.class);

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final BottlingBatchRepository bottlingBatchRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentUnitRepository shipmentUnitRepository;
    private final BottledUnitRepository bottledUnitRepository;
    private final RecallRepository recallRepository;
    private final RecallUnitRepository recallUnitRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public InventoryMovementService(
            InventoryMovementRepository inventoryMovementRepository,
            InventoryLocationRepository inventoryLocationRepository,
            BottlingBatchRepository bottlingBatchRepository,
            ShipmentRepository shipmentRepository,
            ShipmentItemRepository shipmentItemRepository,
            ShipmentUnitRepository shipmentUnitRepository,
            BottledUnitRepository bottledUnitRepository,
            RecallRepository recallRepository,
            RecallUnitRepository recallUnitRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryLocationRepository = inventoryLocationRepository;
        this.bottlingBatchRepository = bottlingBatchRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.shipmentUnitRepository = shipmentUnitRepository;
        this.bottledUnitRepository = bottledUnitRepository;
        this.recallRepository = recallRepository;
        this.recallUnitRepository = recallUnitRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * FR-35 / RB-506: Registra un movimiento en el Kárdex de inventario.
     */
    @Transactional
    public InventoryMovementResponse create(InventoryMovementCreateRequest request, UUID currentUserId) {
        log.debug("Recording inventory movement: type={} batch={} location={} change={}",
                request.movementType(), request.bottlingBatchId(), request.locationId(), request.quantityChangeUnits());
        if (request.bottlingBatchId() == null) {
            throw new BusinessRuleViolationException("RB-506", "bottlingBatchId is required");
        }

        // 1. Bloqueo determinista del lote de envasado
        BottlingBatch bottlingBatch = bottlingBatchRepository.findByIdForUpdate(request.bottlingBatchId())
                .orElseThrow(() -> NotFoundException.of("BottlingBatch", request.bottlingBatchId()));

        // 2. Validación de la ubicación física
        if (request.locationId() == null) {
            throw new BusinessRuleViolationException("RB-506", "locationId is required");
        }
        InventoryLocation location = inventoryLocationRepository.findById(request.locationId())
                .orElseThrow(() -> NotFoundException.of("InventoryLocation", request.locationId()));
        if (!Boolean.TRUE.equals(location.getActive())) {
            throw new BusinessRuleViolationException("RB-506", "Inventory location is not active: " + location.getCode());
        }

        // 3. Validación de semántica de signos según MovementType
        if (request.movementType() == null) {
            throw new BusinessRuleViolationException("RB-506", "movementType is required");
        }
        if (request.quantityChangeUnits() == null) {
            throw new BusinessRuleViolationException("RB-506", "quantityChangeUnits is required");
        }

        int change = request.quantityChangeUnits();
        switch (request.movementType()) {
            case INITIAL -> {
                if (change <= 0) throw new BusinessRuleViolationException("RB-506", "INITIAL quantityChangeUnits must be > 0");
            }
            case PRODUCTION_IN -> {
                if (change <= 0) throw new BusinessRuleViolationException("RB-506", "PRODUCTION_IN quantityChangeUnits must be > 0");
            }
            case RETURN_IN -> {
                if (change <= 0) throw new BusinessRuleViolationException("RB-506", "RETURN_IN quantityChangeUnits must be > 0");
            }
            case SHIPMENT_OUT -> {
                if (change >= 0) throw new BusinessRuleViolationException("RB-506", "SHIPMENT_OUT quantityChangeUnits must be < 0");
            }
            case LOSS_OUT -> {
                if (change >= 0) throw new BusinessRuleViolationException("RB-506", "LOSS_OUT quantityChangeUnits must be < 0");
                if (request.reason() == null || request.reason().isBlank()) {
                    throw new BusinessRuleViolationException("RB-506", "LOSS_OUT requires a non-blank reason");
                }
            }
            case RECALL_OUT -> {
                if (change >= 0) throw new BusinessRuleViolationException("RB-506", "RECALL_OUT quantityChangeUnits must be < 0");
                if (!"RECALL".equals(request.referenceType()) || request.referenceId() == null) {
                    throw new BusinessRuleViolationException("RB-506", "RECALL_OUT requires referenceType = 'RECALL' and a non-null referenceId");
                }
            }
            case ADJUSTMENT -> {
                if (change == 0) throw new BusinessRuleViolationException("RB-506", "ADJUSTMENT quantityChangeUnits must be != 0");
                if (request.reason() == null || request.reason().isBlank()) {
                    throw new BusinessRuleViolationException("RB-506", "ADJUSTMENT requires a non-blank reason");
                }
            }
        }

        // 4. Validaciones específicas para SHIPMENT_OUT
        if (request.movementType() == MovementType.SHIPMENT_OUT && "SHIPMENT".equals(request.referenceType())) {
            if (request.referenceId() == null) {
                throw new BusinessRuleViolationException("RB-506", "referenceId is required when referenceType is SHIPMENT");
            }
            shipmentRepository.findById(request.referenceId())
                    .orElseThrow(() -> NotFoundException.of("Shipment", request.referenceId()));
            ShipmentItem item = shipmentItemRepository.findById(new ShipmentItemId(request.referenceId(), request.bottlingBatchId()))
                    .orElseThrow(() -> new BusinessRuleViolationException("RB-506",
                            "Shipment " + request.referenceId() + " does not contain an item for bottling batch " + request.bottlingBatchId()));

            int alreadyRecorded = Math.abs(inventoryMovementRepository.sumQuantityChangeUnitsByBatchAndReferenceAndType(
                    request.bottlingBatchId(), "SHIPMENT", request.referenceId(), MovementType.SHIPMENT_OUT));
            if (alreadyRecorded + Math.abs(change) > item.getQuantityUnits()) {
                throw new BusinessRuleViolationException("RB-506",
                        "Total accumulated SHIPMENT_OUT (" + (alreadyRecorded + Math.abs(change)) +
                        ") exceeds shipment item quantity (" + item.getQuantityUnits() + ")");
            }
        }

        // 5. Validaciones específicas para RECALL_OUT
        if (request.movementType() == MovementType.RECALL_OUT) {
            Recall recall = recallRepository.findById(request.referenceId())
                    .orElseThrow(() -> NotFoundException.of("Recall", request.referenceId()));

            long totalRecallUnitsForBatch = recallUnitRepository.countByRecallIdAndBottlingBatchId(
                    recall.getId(), request.bottlingBatchId());
            if (totalRecallUnitsForBatch == 0) {
                throw new BusinessRuleViolationException("RB-506",
                        "Recall " + recall.getId() + " does not affect bottling batch " + request.bottlingBatchId());
            }

            int alreadyRecorded = Math.abs(inventoryMovementRepository.sumQuantityChangeUnitsByBatchAndReferenceAndType(
                    request.bottlingBatchId(), "RECALL", request.referenceId(), MovementType.RECALL_OUT));
            if (alreadyRecorded + Math.abs(change) > totalRecallUnitsForBatch) {
                throw new BusinessRuleViolationException("RB-506",
                        "Total accumulated RECALL_OUT (" + (alreadyRecorded + Math.abs(change)) +
                        ") exceeds affected recall units for batch (" + totalRecallUnitsForBatch + ")");
            }
        }

        // 6. Caso especial RETURN_IN con referenceType = "SHIPMENT" (Devolución física)
        if (request.movementType() == MovementType.RETURN_IN && "SHIPMENT".equals(request.referenceType())) {
            if (request.referenceId() == null) {
                throw new BusinessRuleViolationException("RB-506", "referenceId (shipmentId) is required for RETURN_IN from SHIPMENT");
            }
            if (request.bottledUnitIds() == null || request.bottledUnitIds().isEmpty()) {
                throw new BusinessRuleViolationException("RB-506", "bottledUnitIds is required and cannot be empty for RETURN_IN from SHIPMENT");
            }
            Set<UUID> uniqueBottleIds = new HashSet<>(request.bottledUnitIds());
            if (uniqueBottleIds.size() != request.bottledUnitIds().size()) {
                throw new BusinessRuleViolationException("RB-506", "Duplicate bottle IDs provided in RETURN_IN request");
            }
            if (!request.quantityChangeUnits().equals(request.bottledUnitIds().size())) {
                throw new BusinessRuleViolationException("RB-506",
                        "quantityChangeUnits must exactly match bottledUnitIds size (" + request.bottledUnitIds().size() + ")");
            }

            // Protocolo de locks: BottlingBatch (ya bloqueado) -> Shipment -> BottledUnit (UUID ASC)
            Shipment shipment = shipmentRepository.findByIdForUpdate(request.referenceId())
                    .orElseThrow(() -> NotFoundException.of("Shipment", request.referenceId()));

            if (shipment.getStatus() != ShipmentStatus.CANCELLED) {
                throw new BusinessRuleViolationException("RB-506",
                        "RETURN_IN requires shipment status to be CANCELLED (current: " + shipment.getStatus() + ")");
            }

            List<UUID> sortedBottleIds = request.bottledUnitIds().stream().sorted().toList();
            List<BottledUnit> bottles = bottledUnitRepository.findAllByIdInForUpdate(sortedBottleIds);
            if (bottles.size() != sortedBottleIds.size()) {
                throw new BusinessRuleViolationException("RB-506", "One or more bottled units not found");
            }

            List<ShipmentUnit> activeShipmentUnits = shipmentUnitRepository.findByBottledUnit_IdInAndReleasedAtIsNull(sortedBottleIds);
            Map<UUID, ShipmentUnit> shipmentUnitByBottleId = activeShipmentUnits.stream()
                    .collect(Collectors.toMap(su -> su.getBottledUnit().getId(), su -> su));

            for (BottledUnit bottle : bottles) {
                if (!bottle.getBottlingBatch().getId().equals(request.bottlingBatchId())) {
                    throw new BusinessRuleViolationException("RB-506",
                            "Bottled unit " + bottle.getId() + " does not belong to bottling batch " + request.bottlingBatchId());
                }
                if (bottle.getStatus() != BottledUnitStatus.SHIPPED) {
                    throw new BusinessRuleViolationException("RB-506",
                            "Bottled unit " + bottle.getId() + " status is not SHIPPED (current: " + bottle.getStatus() + ")");
                }
                ShipmentUnit su = shipmentUnitByBottleId.get(bottle.getId());
                if (su == null || !su.getShipment().getId().equals(shipment.getId())) {
                    throw new BusinessRuleViolationException("RB-506",
                            "Bottled unit " + bottle.getId() + " does not have an active shipment unit for shipment " + shipment.getId());
                }
            }

            Instant now = Instant.now();
            for (BottledUnit bottle : bottles) {
                bottle.setStatus(BottledUnitStatus.AVAILABLE);
                ShipmentUnit su = shipmentUnitByBottleId.get(bottle.getId());
                su.setReleasedAt(now);
            }
            bottledUnitRepository.saveAll(bottles);
            shipmentUnitRepository.saveAll(activeShipmentUnits);
        }

        // 7. Cálculo de existencias actuales y validación de stock no negativo (RB-506)
        int currentStock = inventoryMovementRepository.sumQuantityChangeUnitsByBottlingBatchIdAndLocationId(
                request.bottlingBatchId(), request.locationId());
        if (change < 0 && (currentStock + change) < 0) {
            log.warn("RB-506: rejected movement — insufficient stock for batch {} in location {}: current={}, change={}",
                    request.bottlingBatchId(), location.getCode(), currentStock, change);
            throw new BusinessRuleViolationException("RB-506",
                    "Insufficient inventory stock in location " + location.getCode() + ": current=" + currentStock + ", change=" + change);
        }
        int resultingStockLevel = currentStock + change;

        // 8. Persistencia inmutable del movimiento
        InventoryMovement movement = new InventoryMovement();
        movement.setBottlingBatch(bottlingBatch);
        movement.setLocation(location);
        movement.setMovementType(request.movementType());
        movement.setQuantityChangeUnits(change);
        movement.setReferenceType(request.referenceType());
        movement.setReferenceId(request.referenceId());
        movement.setReason(request.reason());
        if (currentUserId != null) {
            movement.setCreatedBy(appUserRepository.findById(currentUserId).orElse(null));
        }
        movement.setCreatedAt(Instant.now());
        InventoryMovement saved = inventoryMovementRepository.save(movement);

        // 9. Auditoría CREATE
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("bottlingBatchId", saved.getBottlingBatch().getId());
        after.put("locationId", saved.getLocation().getId());
        after.put("movementType", saved.getMovementType().name());
        after.put("quantityChangeUnits", saved.getQuantityChangeUnits());
        after.put("referenceType", saved.getReferenceType());
        after.put("referenceId", saved.getReferenceId());
        after.put("reason", saved.getReason());
        after.put("resultingStockLevel", resultingStockLevel);

        auditLogService.record(currentUserId, "CREATE", "inventory_movement", saved.getId(), null, toJson(after));
        log.info("Inventory movement {} recorded: {} {} at {} -> resulting stock {}",
                saved.getId(), saved.getMovementType(), change, location.getCode(), resultingStockLevel);

        return toResponse(saved);
    }

    /**
     * FR-35: Consulta de movimientos de inventario con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> listMovements(UUID bottlingBatchId, UUID locationId) {
        List<InventoryMovement> movements;
        if (bottlingBatchId != null && locationId != null) {
            movements = inventoryMovementRepository.findByBottlingBatchIdAndLocationIdWithDetails(bottlingBatchId, locationId);
        } else if (bottlingBatchId != null) {
            movements = inventoryMovementRepository.findByBottlingBatchIdWithDetails(bottlingBatchId);
        } else if (locationId != null) {
            movements = inventoryMovementRepository.findByLocationIdWithDetails(locationId);
        } else {
            movements = inventoryMovementRepository.findAllWithDetails();
        }
        return movements.stream().map(this::toResponse).toList();
    }

    /**
     * FR-36 / RB-506: Existencias actuales calculadas a partir del Kárdex por lote de envasado y ubicación.
     */
    @Transactional(readOnly = true)
    public List<InventoryStockResponse> getStock(UUID bottlingBatchId, UUID locationId) {
        List<BatchLocationStockProjection> projections;
        if (bottlingBatchId != null && locationId != null) {
            projections = inventoryMovementRepository.findStockSummaryByBatchAndLocation(bottlingBatchId, locationId);
        } else if (bottlingBatchId != null) {
            projections = inventoryMovementRepository.findStockSummaryByBatch(bottlingBatchId);
        } else if (locationId != null) {
            projections = inventoryMovementRepository.findStockSummaryByLocation(locationId);
        } else {
            projections = inventoryMovementRepository.findStockSummaryAll();
        }

        return projections.stream()
                .map(p -> new InventoryStockResponse(
                        p.getBottlingBatchId(),
                        p.getProductionLotNumber(),
                        p.getLocationId(),
                        p.getLocationCode(),
                        p.getLocationName(),
                        p.getCurrentStockUnits() != null ? p.getCurrentStockUnits().intValue() : 0,
                        p.getLastMovementAt()
                ))
                .toList();
    }

    /**
     * FR-36 / RB-506: Reconciliación de inventario y detección de discrepancias para un lote de envasado.
     */
    @Transactional(readOnly = true)
    public InventoryReconciliationResponse reconcile(UUID bottlingBatchId) {
        if (bottlingBatchId == null) {
            throw new BusinessRuleViolationException("RB-506", "bottlingBatchId is required");
        }

        BottlingBatch bottlingBatch = bottlingBatchRepository.findById(bottlingBatchId)
                .orElseThrow(() -> NotFoundException.of("BottlingBatch", bottlingBatchId));

        // 1. Kárdex total en todas las ubicaciones para este batch
        Integer kardexTotal = inventoryMovementRepository.sumQuantityChangeUnitsByBottlingBatchId(bottlingBatchId);
        int kardexTotalStock = kardexTotal != null ? kardexTotal : 0;

        // 2. Conteo de unidades físicas agrupado por status (batch completo)
        Map<BottledUnitStatus, Long> countsByStatus = new EnumMap<>(BottledUnitStatus.class);
        for (BottledUnitStatus status : BottledUnitStatus.values()) {
            countsByStatus.put(status, 0L);
        }

        List<Object[]> rows = bottledUnitRepository.countGroupByStatusForBatch(bottlingBatchId);
        long totalBottledUnitRows = 0L;
        for (Object[] row : rows) {
            BottledUnitStatus status = (BottledUnitStatus) row[0];
            Long count = (Long) row[1];
            countsByStatus.put(status, count);
            totalBottledUnitRows += count;
        }

        // 3. Unidades físicas operativas en almacén (AVAILABLE + RESERVED)
        long available = countsByStatus.get(BottledUnitStatus.AVAILABLE);
        long reserved = countsByStatus.get(BottledUnitStatus.RESERVED);
        long operationalWarehouseUnits = available + reserved;

        // 4. Discrepancia en almacén (Kárdex vs botellas operativas)
        int warehouseDiscrepancyUnits = kardexTotalStock - (int) operationalWarehouseUnits;

        // 5. Discrepancia de producción (Envasadas vs registradas)
        int unitsBottled = bottlingBatch.getUnitsBottled() != null ? bottlingBatch.getUnitsBottled() : 0;
        int productionDiscrepancyUnits = unitsBottled - (int) totalBottledUnitRows;

        // 6. Indicador global de discrepancia
        boolean hasDiscrepancy = (warehouseDiscrepancyUnits != 0) || (productionDiscrepancyUnits != 0);
        if (hasDiscrepancy) {
            log.warn("RB-506: reconciliation discrepancy on batch {} — warehouse={}, production={}",
                    bottlingBatchId, warehouseDiscrepancyUnits, productionDiscrepancyUnits);
        } else {
            log.debug("Reconciliation for batch {} shows no discrepancy", bottlingBatchId);
        }

        return new InventoryReconciliationResponse(
                bottlingBatch.getId(),
                bottlingBatch.getProductionLotNumber(),
                unitsBottled,
                kardexTotalStock,
                operationalWarehouseUnits,
                warehouseDiscrepancyUnits,
                totalBottledUnitRows,
                productionDiscrepancyUnits,
                hasDiscrepancy,
                available,
                reserved,
                countsByStatus.get(BottledUnitStatus.SHIPPED),
                countsByStatus.get(BottledUnitStatus.DELIVERED),
                countsByStatus.get(BottledUnitStatus.RECALLED),
                countsByStatus.get(BottledUnitStatus.LOST),
                countsByStatus.get(BottledUnitStatus.DAMAGED),
                Instant.now()
        );
    }

    private InventoryMovementResponse toResponse(InventoryMovement m) {
        return new InventoryMovementResponse(
                m.getId(),
                m.getBottlingBatch().getId(),
                m.getBottlingBatch().getProductionLotNumber(),
                m.getLocation().getId(),
                m.getLocation().getCode(),
                m.getLocation().getName(),
                m.getMovementType(),
                m.getQuantityChangeUnits(),
                m.getReferenceType(),
                m.getReferenceId(),
                m.getReason(),
                m.getCreatedBy() != null ? m.getCreatedBy().getId() : null,
                m.getCreatedBy() != null ? m.getCreatedBy().getUsername() : null,
                m.getCreatedAt()
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
