package org.dev.tequilacluster.services.logistics;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.logistics.ShipmentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDelayCheckResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentItemRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentItemResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;
import org.dev.tequilacluster.models.catalogs.Carrier;
import org.dev.tequilacluster.models.catalogs.DocumentType;
import org.dev.tequilacluster.models.catalogs.ShipmentType;
import org.dev.tequilacluster.models.catalogs.ValidationRule;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.logistics.ShipmentDocument;
import org.dev.tequilacluster.models.logistics.ShipmentItem;
import org.dev.tequilacluster.models.logistics.ShipmentItemId;
import org.dev.tequilacluster.models.logistics.ShipmentTypeRequiredDocument;
import org.dev.tequilacluster.models.logistics.ShipmentUnit;
import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.catalogs.CarrierRepository;
import org.dev.tequilacluster.repositories.catalogs.DocumentTypeRepository;
import org.dev.tequilacluster.repositories.catalogs.ShipmentTypeRepository;
import org.dev.tequilacluster.repositories.catalogs.ValidationRuleRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentDocumentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentItemRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentTypeRequiredDocumentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentUnitRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Logistics stage service: FR-26 a FR-32, RB-401 a RB-406.
 */
@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ShipmentDocumentRepository shipmentDocumentRepository;
    private final ShipmentTypeRequiredDocumentRepository shipmentTypeRequiredDocumentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final BottlingBatchRepository bottlingBatchRepository;
    private final ShipmentTypeRepository shipmentTypeRepository;
    private final CarrierRepository carrierRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final ValidationRuleRepository validationRuleRepository;
    private final ProcessAlertRepository processAlertRepository;
    private final AlertService alertService;
    private final BottledUnitRepository bottledUnitRepository;
    private final ShipmentUnitRepository shipmentUnitRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            ShipmentItemRepository shipmentItemRepository,
            ShipmentDocumentRepository shipmentDocumentRepository,
            ShipmentTypeRequiredDocumentRepository shipmentTypeRequiredDocumentRepository,
            DocumentTypeRepository documentTypeRepository,
            BottlingBatchRepository bottlingBatchRepository,
            ShipmentTypeRepository shipmentTypeRepository,
            CarrierRepository carrierRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            ValidationRuleRepository validationRuleRepository,
            ProcessAlertRepository processAlertRepository,
            AlertService alertService,
            BottledUnitRepository bottledUnitRepository,
            ShipmentUnitRepository shipmentUnitRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.shipmentDocumentRepository = shipmentDocumentRepository;
        this.shipmentTypeRequiredDocumentRepository = shipmentTypeRequiredDocumentRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.bottlingBatchRepository = bottlingBatchRepository;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.carrierRepository = carrierRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.validationRuleRepository = validationRuleRepository;
        this.processAlertRepository = processAlertRepository;
        this.alertService = alertService;
        this.bottledUnitRepository = bottledUnitRepository;
        this.shipmentUnitRepository = shipmentUnitRepository;
    }

    /**
     * FR-26 a FR-28, RB-401, RB-403, RB-406:
     * Crea un nuevo embarque en estado PLANNED.
     */
    @Transactional
    public ShipmentResponse create(ShipmentCreateRequest request, UUID currentUserId) {
        // RB-403: departureAt <= estimatedArrivalAt
        if (request.departureAt().isAfter(request.estimatedArrivalAt())) {
            throw new BusinessRuleViolationException("RB-403",
                    "Departure date must be before or equal to estimated arrival date");
        }

        // RB-403: shipment_number único
        if (shipmentRepository.existsByShipmentNumber(request.shipmentNumber())) {
            throw new BusinessRuleViolationException("RB-403",
                    "Shipment number already exists: " + request.shipmentNumber());
        }

        // Validar tipo de embarque existente y activo
        ShipmentType shipmentType = shipmentTypeRepository.findById(request.shipmentTypeId())
                .orElseThrow(() -> NotFoundException.of("ShipmentType", request.shipmentTypeId()));
        if (!Boolean.TRUE.equals(shipmentType.getActive())) {
            throw new BusinessRuleViolationException("RB-402",
                    "Shipment type is inactive: " + request.shipmentTypeId());
        }

        // Validar transportista existente y activo
        Carrier carrier = carrierRepository.findById(request.carrierId())
                .orElseThrow(() -> NotFoundException.of("Carrier", request.carrierId()));
        if (!Boolean.TRUE.equals(carrier.getActive())) {
            throw new BusinessRuleViolationException("RB-403",
                    "Carrier is inactive: " + request.carrierId());
        }

        // Validar items no vacíos
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessRuleViolationException("RB-401",
                    "Shipment must contain at least one item");
        }

        // Consolidar cantidades solicitadas por batch
        Map<UUID, Integer> requestedUnitsByBatch = request.items().stream()
                .collect(Collectors.groupingBy(
                        ShipmentItemRequest::bottlingBatchId,
                        Collectors.summingInt(ShipmentItemRequest::quantityUnits)
                ));

        // Validar que cada cantidad solicitada sea positiva
        for (Map.Entry<UUID, Integer> entry : requestedUnitsByBatch.entrySet()) {
            if (entry.getValue() <= 0) {
                throw new BusinessRuleViolationException("RB-401",
                        "Requested quantity must be positive for batch: " + entry.getKey());
            }
        }

        // Obtener IDs distintos y ordenarlos de forma determinista por UUID para evitar deadlocks
        List<UUID> sortedBatchIds = requestedUnitsByBatch.keySet().stream()
                .sorted()
                .toList();

        // Adquirir lock PESSIMISTIC_WRITE sobre cada BottlingBatch en orden determinista
        Map<UUID, BottlingBatch> lockedBatches = new HashMap<>();
        Map<UUID, List<BottledUnit>> bottlesByBatch = new HashMap<>();

        for (UUID batchId : sortedBatchIds) {
            BottlingBatch bottlingBatch = bottlingBatchRepository.findByIdForUpdate(batchId)
                    .orElseThrow(() -> NotFoundException.of("BottlingBatch", batchId));
            lockedBatches.put(batchId, bottlingBatch);

            // FR-26 / RB-401: solo bottling batches COMPLETED
            Batch parentBatch = bottlingBatch.getBatch();
            if (parentBatch.getStatus() != BatchStatus.COMPLETED) {
                throw new BusinessRuleViolationException("RB-401",
                        "Source bottling batch is not COMPLETED: " + batchId);
            }

            Integer requestedQuantity = requestedUnitsByBatch.get(batchId);

            // Consultar exactamente quantityUnits de BottledUnit con status AVAILABLE (orden determinista por unitCode)
            List<BottledUnit> availableBottles = bottledUnitRepository.findAvailableForReservation(
                    batchId,
                    BottledUnitStatus.AVAILABLE,
                    PageRequest.of(0, requestedQuantity)
            );

            // FR-27 / RB-401 / RB-406: rechazar si hay menos botellas físicas disponibles que las solicitadas
            if (availableBottles.size() < requestedQuantity) {
                throw new BusinessRuleViolationException("RB-401",
                        "Requested units (" + requestedQuantity + ") exceeds available units ("
                                + availableBottles.size() + ") for bottling batch: " + batchId);
            }

            bottlesByBatch.put(batchId, availableBottles);
        }

        // Persistir Shipment en estado PLANNED
        Shipment shipment = new Shipment();
        shipment.setShipmentNumber(request.shipmentNumber());
        shipment.setShipmentType(shipmentType);
        shipment.setCarrier(carrier);
        shipment.setVehicleLicensePlate(request.vehicleLicensePlate());
        shipment.setDestination(request.destination());
        shipment.setDepartureAt(request.departureAt());
        shipment.setEstimatedArrivalAt(request.estimatedArrivalAt());
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreatedAt(Instant.now());
        if (currentUserId != null) {
            AppUser userRef = appUserRepository.getReferenceById(currentUserId);
            shipment.setCreatedBy(userRef);
        }
        shipment = shipmentRepository.save(shipment);

        // Persistir ShipmentItems consolidados y reservar físicamente cada BottledUnit con ShipmentUnit activa
        List<ShipmentItem> savedItems = new ArrayList<>();
        List<BottledUnit> bottlesToSave = new ArrayList<>();
        List<ShipmentUnit> unitsToSave = new ArrayList<>();
        Instant reservationInstant = Instant.now();

        for (UUID batchId : sortedBatchIds) {
            BottlingBatch bb = lockedBatches.get(batchId);
            Integer qty = requestedUnitsByBatch.get(batchId);
            ShipmentItemId itemId = new ShipmentItemId(shipment.getId(), bb.getId());
            ShipmentItem item = new ShipmentItem(itemId, shipment, bb, qty);
            savedItems.add(shipmentItemRepository.save(item));

            List<BottledUnit> batchBottles = bottlesByBatch.get(batchId);
            for (BottledUnit bottle : batchBottles) {
                if (bottle.getStatus() != BottledUnitStatus.AVAILABLE) {
                    throw new BusinessRuleViolationException("RB-401",
                            "Bottle " + bottle.getUnitCode() + " is not AVAILABLE for reservation");
                }
                bottle.setStatus(BottledUnitStatus.RESERVED);
                bottlesToSave.add(bottle);

                ShipmentUnit unit = ShipmentUnit.builder()
                        .shipment(shipment)
                        .bottledUnit(bottle)
                        .assignedAt(reservationInstant)
                        .releasedAt(null)
                        .build();
                unitsToSave.add(unit);
            }
        }

        bottledUnitRepository.saveAll(bottlesToSave);
        shipmentUnitRepository.saveAll(unitsToSave);

        // FR-42: Auditoría serializada con ObjectMapper (datos agregados seguros, sin arrays de miles de UUIDs)
        int totalUnitsReserved = savedItems.stream().mapToInt(ShipmentItem::getQuantityUnits).sum();
        Map<String, Object> after = Map.of(
                "shipmentNumber", shipment.getShipmentNumber(),
                "status", shipment.getStatus().name(),
                "totalUnitsReserved", totalUnitsReserved
        );
        auditLogService.record(currentUserId, "CREATE", "shipment", shipment.getId(), null, toJson(after));

        return toResponse(shipment, savedItems, List.of());
    }

    /**
     * FR-29: Agrega un documento de respaldo a un embarque.
     */
    @Transactional
    public ShipmentDocumentResponse addDocument(UUID shipmentId, ShipmentDocumentCreateRequest request, UUID currentUserId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));

        // Impedir agregar documentos si el shipment está DELIVERED o CANCELLED
        if (shipment.getStatus() == ShipmentStatus.DELIVERED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("RB-402",
                    "Cannot add documents to a shipment in status: " + shipment.getStatus());
        }

        DocumentType documentType = documentTypeRepository.findById(request.documentTypeId())
                .orElseThrow(() -> NotFoundException.of("DocumentType", request.documentTypeId()));

        if (!Boolean.TRUE.equals(documentType.getActive())) {
            throw new BusinessRuleViolationException("RB-402",
                    "Document type is inactive: " + request.documentTypeId());
        }

        // Restricción de unicidad: UNIQUE (shipment_id, document_type_id, document_number)
        if (request.documentNumber() != null && shipmentDocumentRepository.existsByShipment_IdAndDocumentType_IdAndDocumentNumber(
                shipmentId, request.documentTypeId(), request.documentNumber())) {
            throw new BusinessRuleViolationException("RB-402",
                    "Document with type " + documentType.getCode() + " and number " + request.documentNumber()
                            + " already exists for shipment: " + shipment.getShipmentNumber());
        }

        ShipmentDocument doc = new ShipmentDocument();
        doc.setShipment(shipment);
        doc.setDocumentType(documentType);
        doc.setDocumentNumber(request.documentNumber());
        doc.setFileUrl(request.fileUrl());
        doc.setValid(request.valid() != null ? request.valid() : true);
        doc.setUploadedAt(Instant.now());
        if (currentUserId != null) {
            doc.setUploadedBy(appUserRepository.getReferenceById(currentUserId));
        }
        doc = shipmentDocumentRepository.save(doc);

        // FR-42: Auditoría serializada con ObjectMapper
        Map<String, Object> after = new HashMap<>();
        after.put("documentTypeId", doc.getDocumentType().getId());
        after.put("documentNumber", doc.getDocumentNumber());
        after.put("valid", doc.getValid());
        auditLogService.record(currentUserId, "ADD_DOCUMENT", "shipment_document", doc.getId(), null, toJson(after));

        return toDocumentResponse(doc);
    }

    /**
     * FR-29 / RB-402, RB-405:
     * Transición PLANNED -> IN_TRANSIT tras verificar todos los documentos requeridos válidos
     * y validar la integridad física de las unidades (RESERVED -> SHIPPED).
     */
    @Transactional
    public ShipmentResponse startTransit(UUID shipmentId, UUID currentUserId) {
        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.PLANNED) {
            throw new BusinessRuleViolationException("RB-405",
                    "Shipment can only transition to IN_TRANSIT from PLANNED. Current status: " + shipment.getStatus());
        }

        // Obtener documentos requeridos para el tipo de embarque (únicamente required = true)
        List<ShipmentTypeRequiredDocument> requiredDocs = shipmentTypeRequiredDocumentRepository
                .findByShipmentType_IdAndRequiredTrue(shipment.getShipmentType().getId());

        List<ShipmentDocument> uploadedDocs = shipmentDocumentRepository.findByShipment_Id(shipmentId);

        // Verificar que para cada DocumentType obligatorio exista al menos un ShipmentDocument y sea válido
        for (ShipmentTypeRequiredDocument req : requiredDocs) {
            DocumentType reqDocType = req.getDocumentType();
            List<ShipmentDocument> matchingDocs = uploadedDocs.stream()
                    .filter(d -> d.getDocumentType().getId().equals(reqDocType.getId()))
                    .toList();

            if (matchingDocs.isEmpty()) {
                throw new BusinessRuleViolationException("RB-402",
                        "Missing required document: " + reqDocType.getName() + " (" + reqDocType.getCode() + ")");
            }

            boolean hasValidDoc = matchingDocs.stream()
                    .anyMatch(d -> Boolean.TRUE.equals(d.getValid()));
            if (!hasValidDoc) {
                throw new BusinessRuleViolationException("RB-402",
                        "Required document is invalid: " + reqDocType.getName() + " (" + reqDocType.getCode() + ")");
            }
        }

        // Validar integridad física: consultar ShipmentUnit activas asociadas al shipment
        List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(shipmentId);
        if (items.isEmpty()) {
            throw new BusinessRuleViolationException("RB-401",
                    "Shipment has no items associated: " + shipmentId);
        }

        List<ShipmentUnit> activeUnits = shipmentUnitRepository.findByShipment_IdAndReleasedAtIsNull(shipmentId);

        Map<UUID, List<ShipmentUnit>> unitsByBatch = activeUnits.stream()
                .collect(Collectors.groupingBy(su -> su.getBottledUnit().getBottlingBatch().getId()));

        for (ShipmentItem item : items) {
            List<ShipmentUnit> batchUnits = unitsByBatch.getOrDefault(item.getBottlingBatch().getId(), List.of());
            if (batchUnits.size() != item.getQuantityUnits()) {
                throw new BusinessRuleViolationException("RB-401",
                        "Data integrity violation: shipment item requires " + item.getQuantityUnits()
                                + " units for batch " + item.getBottlingBatch().getId()
                                + ", but found " + batchUnits.size() + " active shipment units.");
            }
        }

        int totalItemUnits = items.stream().mapToInt(ShipmentItem::getQuantityUnits).sum();
        if (activeUnits.size() != totalItemUnits) {
            throw new BusinessRuleViolationException("RB-401",
                    "Data integrity violation: total active shipment units (" + activeUnits.size()
                            + ") does not match total item quantity (" + totalItemUnits + ").");
        }

        // Verificar que todas las botellas asociadas estén en RESERVED y pasar a SHIPPED
        List<BottledUnit> bottlesToShip = new ArrayList<>();
        for (ShipmentUnit unit : activeUnits) {
            BottledUnit bottle = unit.getBottledUnit();
            if (bottle.getStatus() != BottledUnitStatus.RESERVED) {
                throw new BusinessRuleViolationException("RB-405",
                        "Bottled unit " + bottle.getUnitCode() + " is in status " + bottle.getStatus()
                                + ", expected RESERVED to start transit.");
            }
            bottle.setStatus(BottledUnitStatus.SHIPPED);
            bottlesToShip.add(bottle);
        }
        bottledUnitRepository.saveAll(bottlesToShip);

        // Cambiar estado a IN_TRANSIT
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment = shipmentRepository.save(shipment);

        // FR-42: Auditoría serializada con ObjectMapper
        Map<String, Object> before = Map.of("status", ShipmentStatus.PLANNED.name());
        Map<String, Object> after = Map.of("status", ShipmentStatus.IN_TRANSIT.name());
        auditLogService.record(currentUserId, "START_TRANSIT", "shipment", shipment.getId(), toJson(before), toJson(after));

        return toResponse(shipment, items, uploadedDocs);
    }

    /**
     * FR-32 / RB-405: Cancela un embarque en estado PLANNED o IN_TRANSIT.
     * Un embarque DELIVERED no puede cancelarse.
     * Un embarque CANCELLED no puede volver a cancelarse.
     *
     * Si cancela desde PLANNED:
     * - Valida estrictamente que las botellas asociadas estén en RESERVED (aborta si hay inconsistencia).
     * - Las botellas pasan de RESERVED -> AVAILABLE.
     * - ShipmentUnit.releasedAt = now() (sin borrado físico).
     *
     * Si cancela desde IN_TRANSIT:
     * - Las botellas permanecen en SHIPPED (custodia del transportista hasta que se registre retorno físico en Inventory).
     * - ShipmentUnit.releasedAt permanece null.
     *
     * Limitación de modelo: La entidad y tabla 'shipment' no cuentan con columna
     * para 'cancellation_reason'. El motivo obligatorio se preserva en el
     * log de auditoría 'afterData' mediante serialización JSON.
     */
    @Transactional
    public ShipmentResponse cancel(UUID shipmentId, UUID currentUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("RB-405", "Cancellation reason is required");
        }

        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new BusinessRuleViolationException("RB-405",
                    "Cannot cancel a shipment that has already been delivered");
        }

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("RB-405",
                    "Shipment is already cancelled");
        }

        ShipmentStatus previousStatus = shipment.getStatus();

        if (previousStatus == ShipmentStatus.PLANNED) {
            // Cancelación segura antes del despacho: liberar reserva física
            List<ShipmentUnit> activeUnits = shipmentUnitRepository.findByShipment_IdAndReleasedAtIsNull(shipmentId);
            Instant now = Instant.now();
            List<BottledUnit> bottlesToRelease = new ArrayList<>();

            for (ShipmentUnit unit : activeUnits) {
                BottledUnit bottle = unit.getBottledUnit();
                if (bottle.getStatus() != BottledUnitStatus.RESERVED) {
                    throw new BusinessRuleViolationException("RB-405",
                            "Data integrity violation: Bottled unit " + bottle.getUnitCode()
                                    + " is in status " + bottle.getStatus()
                                    + ", expected RESERVED to cancel from PLANNED.");
                }
                bottle.setStatus(BottledUnitStatus.AVAILABLE);
                bottlesToRelease.add(bottle);
                unit.setReleasedAt(now);
            }

            bottledUnitRepository.saveAll(bottlesToRelease);
            shipmentUnitRepository.saveAll(activeUnits);
        }
        // Si cancela desde IN_TRANSIT: las botellas quedan en SHIPPED y releasedAt en null
        // hasta que Inventory procese el RETURN_IN físico.

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment = shipmentRepository.save(shipment);

        // FR-42: Auditoría con estado anterior y posterior (incluyendo motivo obligatorio)
        Map<String, Object> before = Map.of("status", previousStatus.name());
        Map<String, Object> after = Map.of(
                "status", ShipmentStatus.CANCELLED.name(),
                "cancellationReason", reason
        );
        auditLogService.record(currentUserId, "CANCEL", "shipment", shipment.getId(), toJson(before), toJson(after));

        List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(shipmentId);
        List<ShipmentDocument> documents = shipmentDocumentRepository.findByShipment_Id(shipmentId);
        return toResponse(shipment, items, documents);
    }

    /**
     * FR-32 / RB-405: Confirma la entrega de un embarque en destino.
     * Solo permitido desde IN_TRANSIT.
     * Valida integridad física de las unidades (ShipmentUnit activas).
     * Transiciona las botellas de SHIPPED -> DELIVERED.
     * Registra deliveredAt = now() y cambia status a DELIVERED.
     * Las filas de ShipmentUnit conservan releasedAt = null como evidencia permanente de custodia y entrega.
     */
    @Transactional
    public ShipmentResponse deliver(UUID shipmentId, UUID currentUserId) {
        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> NotFoundException.of("Shipment", shipmentId));

        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new BusinessRuleViolationException("RB-405",
                    "Shipment can only transition to DELIVERED from IN_TRANSIT. Current status: " + shipment.getStatus());
        }

        // Validar integridad física: consultar items y ShipmentUnit activas
        List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(shipmentId);
        if (items.isEmpty()) {
            throw new BusinessRuleViolationException("RB-401",
                    "Shipment has no items associated: " + shipmentId);
        }

        List<ShipmentUnit> activeUnits = shipmentUnitRepository.findByShipment_IdAndReleasedAtIsNull(shipmentId);

        Map<UUID, List<ShipmentUnit>> unitsByBatch = activeUnits.stream()
                .collect(Collectors.groupingBy(su -> su.getBottledUnit().getBottlingBatch().getId()));

        for (ShipmentItem item : items) {
            List<ShipmentUnit> batchUnits = unitsByBatch.getOrDefault(item.getBottlingBatch().getId(), List.of());
            if (batchUnits.size() != item.getQuantityUnits()) {
                throw new BusinessRuleViolationException("RB-401",
                        "Data integrity violation: shipment item requires " + item.getQuantityUnits()
                                + " units for batch " + item.getBottlingBatch().getId()
                                + ", but found " + batchUnits.size() + " active shipment units.");
            }
        }

        int totalItemUnits = items.stream().mapToInt(ShipmentItem::getQuantityUnits).sum();
        if (activeUnits.size() != totalItemUnits) {
            throw new BusinessRuleViolationException("RB-401",
                    "Data integrity violation: total active shipment units (" + activeUnits.size()
                            + ") does not match total item quantity (" + totalItemUnits + ").");
        }

        // Verificar que todas las botellas asociadas estén en SHIPPED y pasar a DELIVERED
        List<BottledUnit> bottlesToDeliver = new ArrayList<>();
        for (ShipmentUnit unit : activeUnits) {
            BottledUnit bottle = unit.getBottledUnit();
            if (bottle.getStatus() != BottledUnitStatus.SHIPPED) {
                throw new BusinessRuleViolationException("RB-405",
                        "Bottled unit " + bottle.getUnitCode() + " is in status " + bottle.getStatus()
                                + ", expected SHIPPED to confirm delivery.");
            }
            bottle.setStatus(BottledUnitStatus.DELIVERED);
            bottlesToDeliver.add(bottle);
        }
        bottledUnitRepository.saveAll(bottlesToDeliver);

        // Actualizar shipment
        Instant now = Instant.now();
        shipment.setDeliveredAt(now);
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment = shipmentRepository.save(shipment);

        // FR-42: Auditoría serializada con ObjectMapper
        Map<String, Object> before = Map.of("status", ShipmentStatus.IN_TRANSIT.name());
        Map<String, Object> after = Map.of(
                "status", ShipmentStatus.DELIVERED.name(),
                "deliveredAt", now.toString()
        );
        auditLogService.record(currentUserId, "DELIVER", "shipment", shipment.getId(), toJson(before), toJson(after));

        List<ShipmentDocument> documents = shipmentDocumentRepository.findByShipment_Id(shipmentId);
        return toResponse(shipment, items, documents);
    }

    /**
     * FR-31 / RB-404: Verifica y genera alertas para embarques en tránsito cuyo
     * tiempo actual ha excedido estimatedArrivalAt + tolerancia.
     *
     * La tolerancia se obtiene de validation_rule para la etapa LOGISTICS.
     * Si no existe o no define valor, se aplica el fallback por defecto de 48 horas.
     * Evita duplicados: Si ya existe una alerta abierta de tipo SHIPMENT_DELAY para el
     * embarque, no se genera una nueva.
     */
    @Transactional
    public ShipmentDelayCheckResponse checkEstimatedArrivalAlerts() {
        long toleranceHours = resolveDelayToleranceHours();
        Instant now = Instant.now();
        Instant cutoff = now.minus(Duration.ofHours(toleranceHours));

        // Todos los embarques en tránsito
        List<Shipment> inTransitShipments = shipmentRepository.findByStatus(ShipmentStatus.IN_TRANSIT);

        List<ShipmentDelayCheckResponse.ShipmentDelayAlertItem> alertItems = new ArrayList<>();
        int delayedCount = 0;
        int newAlertsCount = 0;

        for (Shipment shipment : inTransitShipments) {
            // Un embarque está retrasado si now > estimatedArrivalAt + tolerance
            // lo cual equivale a estimatedArrivalAt < now - tolerance (cutoff)
            if (shipment.getEstimatedArrivalAt() != null && shipment.getEstimatedArrivalAt().isBefore(cutoff)) {
                delayedCount++;

                boolean alertAlreadyOpen = processAlertRepository
                        .existsByShipment_IdAndAlertTypeAndResolvedAtIsNull(shipment.getId(), "SHIPMENT_DELAY");

                if (alertAlreadyOpen) {
                    List<ProcessAlert> existing = processAlertRepository
                            .findByShipment_IdAndAlertTypeAndResolvedAtIsNull(shipment.getId(), "SHIPMENT_DELAY");
                    UUID alertId = existing.isEmpty() ? null : existing.getFirst().getId();
                    String msg = existing.isEmpty() ? "Open delay alert exists" : existing.getFirst().getMessage();
                    alertItems.add(new ShipmentDelayCheckResponse.ShipmentDelayAlertItem(
                            alertId,
                            shipment.getId(),
                            shipment.getShipmentNumber(),
                            msg,
                            false
                    ));
                } else {
                    String message = String.format(
                            "Shipment %s is delayed. Estimated arrival was %s (tolerance: %d hours exceeded).",
                            shipment.getShipmentNumber(),
                            shipment.getEstimatedArrivalAt(),
                            toleranceHours
                    );
                    ProcessAlert alert = alertService.raiseForShipment(
                            shipment.getId(),
                            "SHIPMENT_DELAY",
                            AlertSeverity.WARNING,
                            message
                    );
                    newAlertsCount++;
                    alertItems.add(new ShipmentDelayCheckResponse.ShipmentDelayAlertItem(
                            alert.getId(),
                            shipment.getId(),
                            shipment.getShipmentNumber(),
                            alert.getMessage(),
                            true
                    ));
                }
            }
        }

        return new ShipmentDelayCheckResponse(
                inTransitShipments.size(),
                delayedCount,
                newAlertsCount,
                toleranceHours,
                alertItems
        );
    }

    private long resolveDelayToleranceHours() {
        Optional<ValidationRule> ruleOpt = validationRuleRepository
                .findByProcessStageCodeAndParameterCodeAndActiveTrue(ProcessStageCodes.LOGISTICS, "ESTIMATED_ARRIVAL_TOLERANCE");

        if (ruleOpt.isPresent()) {
            ValidationRule rule = ruleOpt.get();
            if (rule.getAllowedDeviation() != null && rule.getAllowedDeviation().compareTo(BigDecimal.ZERO) >= 0) {
                return rule.getAllowedDeviation().longValue();
            }
        }

        // Fallback directo de 48 horas conforme a FR-31 / RB-404
        return 48L;
    }

    /**
     * Obtener un embarque por su ID.
     */
    @Transactional(readOnly = true)
    public ShipmentResponse get(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Shipment", id));
        List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(id);
        List<ShipmentDocument> documents = shipmentDocumentRepository.findByShipment_Id(id);
        return toResponse(shipment, items, documents);
    }

    /**
     * Listar todos los embarques.
     */
    @Transactional(readOnly = true)
    public List<ShipmentResponse> list() {
        return shipmentRepository.findAll().stream()
                .map(shipment -> {
                    List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(shipment.getId());
                    List<ShipmentDocument> documents = shipmentDocumentRepository.findByShipment_Id(shipment.getId());
                    return toResponse(shipment, items, documents);
                })
                .toList();
    }

    /**
     * Calcula las unidades disponibles de un lote de envasado (BottlingBatch).
     *
     * Con la reserva física individual implementada (ShipmentUnit), las botellas reservadas,
     * en tránsito y entregadas tienen estados RESERVED, SHIPPED y DELIVERED respectivamente.
     * Por lo tanto, la fuente de verdad definitiva y determinista bajo el lock del lote es
     * el recuento directo de botellas en estado AVAILABLE.
     */
    public int calculateAvailableUnits(BottlingBatch bottlingBatch) {
        if (bottlingBatch == null || bottlingBatch.getId() == null) {
            return 0;
        }
        return (int) bottledUnitRepository.countByBottlingBatch_IdAndStatus(
                bottlingBatch.getId(),
                BottledUnitStatus.AVAILABLE
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

    private ShipmentDocumentResponse toDocumentResponse(ShipmentDocument doc) {
        return new ShipmentDocumentResponse(
                doc.getId(),
                doc.getDocumentType().getId(),
                doc.getDocumentType().getCode(),
                doc.getDocumentType().getName(),
                doc.getDocumentNumber(),
                doc.getFileUrl(),
                doc.getValid(),
                doc.getUploadedAt(),
                doc.getUploadedBy() != null ? doc.getUploadedBy().getId() : null,
                doc.getUploadedBy() != null ? doc.getUploadedBy().getUsername() : null
        );
    }

    private ShipmentResponse toResponse(Shipment shipment, List<ShipmentItem> items, List<ShipmentDocument> documents) {
        List<ShipmentItemResponse> itemResponses = items.stream()
                .map(item -> new ShipmentItemResponse(
                        item.getBottlingBatch().getId(),
                        item.getBottlingBatch().getProductionLotNumber(),
                        item.getBottlingBatch().getBatch().getTraceabilityCode(),
                        item.getQuantityUnits()
                ))
                .toList();

        List<ShipmentDocumentResponse> documentResponses = documents.stream()
                .map(this::toDocumentResponse)
                .toList();

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getShipmentType().getId(),
                shipment.getShipmentType().getCode(),
                shipment.getShipmentType().getName(),
                shipment.getCarrier().getId(),
                shipment.getCarrier().getName(),
                shipment.getVehicleLicensePlate(),
                shipment.getDestination(),
                shipment.getDepartureAt(),
                shipment.getEstimatedArrivalAt(),
                shipment.getDeliveredAt(),
                shipment.getStatus(),
                shipment.getCreatedBy() != null ? shipment.getCreatedBy().getId() : null,
                shipment.getCreatedBy() != null ? shipment.getCreatedBy().getUsername() : null,
                shipment.getCreatedAt(),
                itemResponses,
                documentResponses
        );
    }
}
