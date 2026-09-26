package org.dev.tequilacluster.repositories.bottling;

import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-21: batch_id es FK 1:1 hacia batch(id); production_lot_number único. */
public interface BottlingBatchRepository extends JpaRepository<BottlingBatch, UUID> {

    Optional<BottlingBatch> findByProductionLotNumber(String productionLotNumber);

    List<BottlingBatch> findByBrandId(UUID brandId);

    List<BottlingBatch> findByCategoryId(UUID categoryId);

    boolean existsByProductionLotNumber(String productionLotNumber);
}
