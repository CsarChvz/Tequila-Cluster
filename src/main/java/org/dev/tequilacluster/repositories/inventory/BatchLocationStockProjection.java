package org.dev.tequilacluster.repositories.inventory;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección agregada para existencias por lote de envasado y ubicación física (FR-36).
 */
public interface BatchLocationStockProjection {
    UUID getBottlingBatchId();
    String getProductionLotNumber();
    UUID getLocationId();
    String getLocationCode();
    String getLocationName();
    Long getCurrentStockUnits();
    Instant getLastMovementAt();
}
