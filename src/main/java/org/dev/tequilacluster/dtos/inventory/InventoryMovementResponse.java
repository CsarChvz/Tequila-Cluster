package org.dev.tequilacluster.dtos.inventory;

import org.dev.tequilacluster.models.inventory.enums.MovementType;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-35: Respuesta con el detalle de un movimiento de inventario.
 */
public record InventoryMovementResponse(
        UUID id,
        UUID bottlingBatchId,
        String productionLotNumber,
        UUID locationId,
        String locationCode,
        String locationName,
        MovementType movementType,
        Integer quantityChangeUnits,
        String referenceType,
        UUID referenceId,
        String reason,
        UUID createdByUserId,
        String createdByUsername,
        Instant createdAt
) {
}
