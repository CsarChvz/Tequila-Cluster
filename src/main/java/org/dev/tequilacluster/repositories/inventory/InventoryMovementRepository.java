package org.dev.tequilacluster.repositories.inventory;

import org.dev.tequilacluster.models.inventory.InventoryMovement;
import org.dev.tequilacluster.models.inventory.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT COALESCE(SUM(m.quantityChangeUnits), 0) FROM InventoryMovement m " +
           "WHERE m.bottlingBatch.id = :bottlingBatchId AND m.location.id = :locationId")
    Integer sumQuantityChangeUnitsByBottlingBatchIdAndLocationId(
            @Param("bottlingBatchId") UUID bottlingBatchId,
            @Param("locationId") UUID locationId);

    @Query("SELECT COALESCE(SUM(m.quantityChangeUnits), 0) FROM InventoryMovement m " +
           "WHERE m.bottlingBatch.id = :bottlingBatchId")
    Integer sumQuantityChangeUnitsByBottlingBatchId(@Param("bottlingBatchId") UUID bottlingBatchId);

    @Query("SELECT COALESCE(SUM(m.quantityChangeUnits), 0) FROM InventoryMovement m " +
           "WHERE m.bottlingBatch.id = :bottlingBatchId AND m.referenceType = :referenceType " +
           "AND m.referenceId = :referenceId AND m.movementType = :movementType")
    Integer sumQuantityChangeUnitsByBatchAndReferenceAndType(
            @Param("bottlingBatchId") UUID bottlingBatchId,
            @Param("referenceType") String referenceType,
            @Param("referenceId") UUID referenceId,
            @Param("movementType") MovementType movementType);

    @Query("SELECT m.bottlingBatch.id AS bottlingBatchId, " +
           "m.bottlingBatch.productionLotNumber AS productionLotNumber, " +
           "m.location.id AS locationId, " +
           "m.location.code AS locationCode, " +
           "m.location.name AS locationName, " +
           "SUM(m.quantityChangeUnits) AS currentStockUnits, " +
           "MAX(m.createdAt) AS lastMovementAt " +
           "FROM InventoryMovement m " +
           "WHERE m.bottlingBatch.id = :bottlingBatchId AND m.location.id = :locationId " +
           "GROUP BY m.bottlingBatch.id, m.bottlingBatch.productionLotNumber, m.location.id, m.location.code, m.location.name " +
           "ORDER BY m.bottlingBatch.productionLotNumber ASC, m.location.code ASC")
    List<BatchLocationStockProjection> findStockSummaryByBatchAndLocation(
            @Param("bottlingBatchId") UUID bottlingBatchId,
            @Param("locationId") UUID locationId);

    @Query("SELECT m.bottlingBatch.id AS bottlingBatchId, " +
           "m.bottlingBatch.productionLotNumber AS productionLotNumber, " +
           "m.location.id AS locationId, " +
           "m.location.code AS locationCode, " +
           "m.location.name AS locationName, " +
           "SUM(m.quantityChangeUnits) AS currentStockUnits, " +
           "MAX(m.createdAt) AS lastMovementAt " +
           "FROM InventoryMovement m " +
           "WHERE m.bottlingBatch.id = :bottlingBatchId " +
           "GROUP BY m.bottlingBatch.id, m.bottlingBatch.productionLotNumber, m.location.id, m.location.code, m.location.name " +
           "ORDER BY m.bottlingBatch.productionLotNumber ASC, m.location.code ASC")
    List<BatchLocationStockProjection> findStockSummaryByBatch(@Param("bottlingBatchId") UUID bottlingBatchId);

    @Query("SELECT m.bottlingBatch.id AS bottlingBatchId, " +
           "m.bottlingBatch.productionLotNumber AS productionLotNumber, " +
           "m.location.id AS locationId, " +
           "m.location.code AS locationCode, " +
           "m.location.name AS locationName, " +
           "SUM(m.quantityChangeUnits) AS currentStockUnits, " +
           "MAX(m.createdAt) AS lastMovementAt " +
           "FROM InventoryMovement m " +
           "WHERE m.location.id = :locationId " +
           "GROUP BY m.bottlingBatch.id, m.bottlingBatch.productionLotNumber, m.location.id, m.location.code, m.location.name " +
           "ORDER BY m.bottlingBatch.productionLotNumber ASC, m.location.code ASC")
    List<BatchLocationStockProjection> findStockSummaryByLocation(@Param("locationId") UUID locationId);

    @Query("SELECT m.bottlingBatch.id AS bottlingBatchId, " +
           "m.bottlingBatch.productionLotNumber AS productionLotNumber, " +
           "m.location.id AS locationId, " +
           "m.location.code AS locationCode, " +
           "m.location.name AS locationName, " +
           "SUM(m.quantityChangeUnits) AS currentStockUnits, " +
           "MAX(m.createdAt) AS lastMovementAt " +
           "FROM InventoryMovement m " +
           "GROUP BY m.bottlingBatch.id, m.bottlingBatch.productionLotNumber, m.location.id, m.location.code, m.location.name " +
           "ORDER BY m.bottlingBatch.productionLotNumber ASC, m.location.code ASC")
    List<BatchLocationStockProjection> findStockSummaryAll();

    @Query("SELECT m FROM InventoryMovement m " +
           "JOIN FETCH m.bottlingBatch bb " +
           "JOIN FETCH m.location loc " +
           "LEFT JOIN FETCH m.createdBy usr " +
           "WHERE bb.id = :bottlingBatchId AND loc.id = :locationId " +
           "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findByBottlingBatchIdAndLocationIdWithDetails(
            @Param("bottlingBatchId") UUID bottlingBatchId,
            @Param("locationId") UUID locationId);

    @Query("SELECT m FROM InventoryMovement m " +
           "JOIN FETCH m.bottlingBatch bb " +
           "JOIN FETCH m.location loc " +
           "LEFT JOIN FETCH m.createdBy usr " +
           "WHERE bb.id = :bottlingBatchId " +
           "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findByBottlingBatchIdWithDetails(@Param("bottlingBatchId") UUID bottlingBatchId);

    @Query("SELECT m FROM InventoryMovement m " +
           "JOIN FETCH m.bottlingBatch bb " +
           "JOIN FETCH m.location loc " +
           "LEFT JOIN FETCH m.createdBy usr " +
           "WHERE loc.id = :locationId " +
           "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findByLocationIdWithDetails(@Param("locationId") UUID locationId);

    @Query("SELECT m FROM InventoryMovement m " +
           "JOIN FETCH m.bottlingBatch bb " +
           "JOIN FETCH m.location loc " +
           "LEFT JOIN FETCH m.createdBy usr " +
           "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findAllWithDetails();
}
