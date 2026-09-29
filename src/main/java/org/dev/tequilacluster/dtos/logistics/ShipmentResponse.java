package org.dev.tequilacluster.dtos.logistics;

import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** FR-28 a FR-32: respuesta completa de un embarque incluyendo items y documentos. */
public record ShipmentResponse(
        UUID id,
        String shipmentNumber,
        UUID shipmentTypeId,
        String shipmentTypeCode,
        String shipmentTypeName,
        UUID carrierId,
        String carrierName,
        String vehicleLicensePlate,
        String destination,
        Instant departureAt,
        Instant estimatedArrivalAt,
        Instant deliveredAt,
        ShipmentStatus status,
        UUID createdById,
        String createdByUsername,
        Instant createdAt,
        List<ShipmentItemResponse> items,
        List<ShipmentDocumentResponse> documents
) {
}
