package org.dev.tequilacluster.dtos.inventory;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-36 / RB-506: Existencias actuales calculadas a partir del Kárdex por lote de envasado y ubicación.
 * Nota: Los conteos de botellas físicas no se desglosan por ubicación debido a que BottledUnit
 * no tiene location_id en el modelo de datos.
 */
public record InventoryStockResponse(
        UUID bottlingBatchId,
        String productionLotNumber,
        UUID locationId,
        String locationCode,
        String locationName,
        Integer currentStockUnits,
        Instant lastMovementAt
) {
}
