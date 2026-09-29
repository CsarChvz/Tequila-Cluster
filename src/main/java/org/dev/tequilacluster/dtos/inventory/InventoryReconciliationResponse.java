package org.dev.tequilacluster.dtos.inventory;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-36 / RB-506: Reporte de reconciliación y discrepancias de inventario para un lote de envasado.
 * <p>
 * Los conteos de unidades físicas corresponden al lote completo (BottlingBatch) y no a una
 * ubicación física específica, ya que BottledUnit carece de columna location_id.
 * <p>
 * Nota operativa: Si un Recall (FR-34) marca botellas en RECALLED antes de registrarse el movimiento
 * físico RECALL_OUT en almacén, se manifestará temporalmente una discrepancia en almacén hasta que
 * el movimiento de Kárdex sea registrado.
 */
public record InventoryReconciliationResponse(
        UUID bottlingBatchId,
        String productionLotNumber,
        Integer unitsBottled,
        Integer kardexTotalStock,
        Long operationalWarehouseUnits,
        Integer warehouseDiscrepancyUnits,
        Long totalBottledUnitRows,
        Integer productionDiscrepancyUnits,
        Boolean hasDiscrepancy,
        Long availableUnits,
        Long reservedUnits,
        Long shippedUnits,
        Long deliveredUnits,
        Long recalledUnits,
        Long lostUnits,
        Long damagedUnits,
        Instant reconciledAt
) {
}
