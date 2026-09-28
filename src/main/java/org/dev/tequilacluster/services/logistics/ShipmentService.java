package org.dev.tequilacluster.services.logistics;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.logistics.ShipmentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentItemRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentItemResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.catalogs.Carrier;
import org.dev.tequilacluster.models.catalogs.DocumentType;
import org.dev.tequilacluster.models.catalogs.ShipmentType;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.logistics.ShipmentDocument;
import org.dev.tequilacluster.models.logistics.ShipmentItem;
import org.dev.tequilacluster.models.logistics.ShipmentItemId;
import org.dev.tequilacluster.models.logistics.ShipmentTypeRequiredDocument;
import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.catalogs.CarrierRepository;
import org.dev.tequilacluster.repositories.catalogs.DocumentTypeRepository;
import org.dev.tequilacluster.repositories.catalogs.ShipmentTypeRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentDocumentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentItemRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentTypeRequiredDocumentRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            ObjectMapper objectMapper
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
        for (UUID batchId : sortedBatchIds) {
            BottlingBatch bottlingBatch = bottlingBatchRepository.findByIdForUpdate(batchId)
                    .orElseThrow(() -> NotFoundException.of("BottlingBatch", batchId));
            lockedBatches.put(batchId, bottlingBatch);
        }

        // Con los locks adquiridos, validar estado y disponibilidad de unidades
        for (UUID batchId : sortedBatchIds) {
            BottlingBatch bottlingBatch = lockedBatches.get(batchId);
            Integer requestedQuantity = requestedUnitsByBatch.get(batchId);

            // FR-26 / RB-401: solo bottling batches COMPLETED
            Batch parentBatch = bottlingBatch.getBatch();
            if (parentBatch.getStatus() != BatchStatus.COMPLETED) {
                throw new BusinessRuleViolationException("RB-401",
                        "Source bottling batch is not COMPLETED: " + batchId);
            }

            // FR-27 / RB-401 / RB-406: recalcular unidades disponibles bajo el lock
            int availableUnits = calculateAvailableUnits(bottlingBatch);
            if (requestedQuantity > availableUnits) {
                throw new BusinessRuleViolationException("RB-401",
                        "Requested units (" + requestedQuantity + ") exceeds available units ("
                                + availableUnits + ") for bottling batch: " + batchId);
            }
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

        // Persistir ShipmentItems consolidados
        List<ShipmentItem> savedItems = new ArrayList<>();
        for (UUID batchId : sortedBatchIds) {
            BottlingBatch bb = lockedBatches.get(batchId);
            ShipmentItemId itemId = new ShipmentItemId(shipment.getId(), bb.getId());
            ShipmentItem item = new ShipmentItem(itemId, shipment, bb, requestedUnitsByBatch.get(batchId));
            savedItems.add(shipmentItemRepository.save(item));
        }

        // FR-42: Auditoría serializada con ObjectMapper
        Map<String, Object> after = Map.of(
                "shipmentNumber", shipment.getShipmentNumber(),
                "status", shipment.getStatus().name()
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
     * Transición PLANNED -> IN_TRANSIT tras verificar todos los documentos requeridos válidos.
     */
    @Transactional
    public ShipmentResponse startTransit(UUID shipmentId, UUID currentUserId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
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

        // Cambiar estado a IN_TRANSIT (sin modificar BottledUnit todavía)
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment = shipmentRepository.save(shipment);

        // FR-42: Auditoría serializada con ObjectMapper
        Map<String, Object> before = Map.of("status", ShipmentStatus.PLANNED.name());
        Map<String, Object> after = Map.of("status", ShipmentStatus.IN_TRANSIT.name());
        auditLogService.record(currentUserId, "START_TRANSIT", "shipment", shipment.getId(), toJson(before), toJson(after));

        List<ShipmentItem> items = shipmentItemRepository.findByShipment_Id(shipmentId);
        return toResponse(shipment, items, uploadedDocs);
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
     * Fórmula de disponibilidad:
     * Unidades Disponibles = Unidades Embotelladas Totales (unitsBottled)
     *                      - Unidades Comprometidas en Embarques Activos (status != CANCELLED)
     */
    public int calculateAvailableUnits(BottlingBatch bottlingBatch) {
        int totalBottled = bottlingBatch.getUnitsBottled() != null ? bottlingBatch.getUnitsBottled() : 0;

        List<ShipmentItem> existingItems = shipmentItemRepository.findByBottlingBatch_Id(bottlingBatch.getId());
        int committedUnits = existingItems.stream()
                .filter(item -> item.getShipment().getStatus() != ShipmentStatus.CANCELLED)
                .mapToInt(ShipmentItem::getQuantityUnits)
                .sum();

        return Math.max(0, totalBottled - committedUnits);
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
