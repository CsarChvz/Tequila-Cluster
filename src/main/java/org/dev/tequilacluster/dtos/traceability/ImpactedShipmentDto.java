package org.dev.tequilacluster.dtos.traceability;

import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary of shipment impacted by forward traceability.
 */
public record ImpactedShipmentDto(
        UUID shipmentId,
        String shipmentNumber,
        ShipmentStatus status,
        String carrierName,
        String destination,
        Instant departureAt,
        Instant deliveredAt,
        UUID bottlingBatchId,
        Integer quantityUnits
) {}
