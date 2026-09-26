package org.dev.tequilacluster.repositories.inventory;

import org.dev.tequilacluster.models.inventory.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-35/36: movimientos de inventario por lote de envasado + ubicación, para calcular existencias y reconciliar. */
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByBottlingBatchIdAndLocationId(UUID bottlingBatchId, UUID locationId);

    List<InventoryMovement> findByBottlingBatchId(UUID bottlingBatchId);

    List<InventoryMovement> findByLocationId(UUID locationId);

    List<InventoryMovement> findByMovementType(String movementType);
}
