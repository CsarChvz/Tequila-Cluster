package org.dev.tequilacluster.repositories.inventory;

import org.dev.tequilacluster.models.inventory.InventoryMovement;
import org.dev.tequilacluster.models.inventory.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-35/36: movimientos de inventario por lote de envasado + ubicación, para calcular existencias y reconciliar. */
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByBottlingBatch_IdAndLocation_Id(UUID bottlingBatchId, UUID locationId);

    List<InventoryMovement> findByBottlingBatch_Id(UUID bottlingBatchId);

    List<InventoryMovement> findByLocation_Id(UUID locationId);

    List<InventoryMovement> findByMovementType(MovementType movementType);

    List<InventoryMovement> findByBottlingBatch_IdAndReferenceTypeAndReferenceIdAndMovementType(
            UUID bottlingBatchId, String referenceType, UUID referenceId, MovementType movementType);

    List<InventoryMovement> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
