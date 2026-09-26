package org.dev.tequilacluster.repositories.bottling;

import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-23/37: unidad física trazable por QR/barcode único, ligada a batch y marbete. */
public interface BottledUnitRepository extends JpaRepository<BottledUnit, UUID> {

    Optional<BottledUnit> findByUnitCode(String unitCode);

    Optional<BottledUnit> findByTaxLabelId(UUID taxLabelId);

    List<BottledUnit> findByBottlingBatchIdAndStatus(UUID bottlingBatchId, String status);

    long countByBottlingBatchIdAndStatus(UUID bottlingBatchId, String status);

    boolean existsByUnitCode(String unitCode);
}
