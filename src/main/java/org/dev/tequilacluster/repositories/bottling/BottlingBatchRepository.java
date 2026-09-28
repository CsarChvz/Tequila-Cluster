package org.dev.tequilacluster.repositories.bottling;

import jakarta.persistence.LockModeType;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-21: batch_id es FK 1:1 hacia batch(id); production_lot_number único. */
public interface BottlingBatchRepository extends JpaRepository<BottlingBatch, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BottlingBatch b WHERE b.id = :id")
    Optional<BottlingBatch> findByIdForUpdate(@Param("id") UUID id);

    Optional<BottlingBatch> findByProductionLotNumber(String productionLotNumber);

    List<BottlingBatch> findByBrand_Id(UUID brandId);

    List<BottlingBatch> findByCategory_Id(UUID categoryId);

    boolean existsByProductionLotNumber(String productionLotNumber);
}
