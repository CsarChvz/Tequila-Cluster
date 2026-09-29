package org.dev.tequilacluster.dtos.inventory;

import jakarta.validation.constraints.NotNull;
import org.dev.tequilacluster.models.inventory.enums.MovementType;

import java.util.List;
import java.util.UUID;

/**
 * FR-35: Solicitud de creación de un movimiento de inventario (Kárdex).
 * El valor de quantityChangeUnits refleja directamente el cambio algebraico real.
 */
public record InventoryMovementCreateRequest(
        @NotNull UUID bottlingBatchId,
        @NotNull UUID locationId,
        @NotNull MovementType movementType,
        @NotNull Integer quantityChangeUnits,
        String referenceType,
        UUID referenceId,
        String reason,
        List<UUID> bottledUnitIds
) {
}
