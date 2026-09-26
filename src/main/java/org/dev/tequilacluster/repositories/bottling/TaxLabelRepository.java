package org.dev.tequilacluster.repositories.bottling;

import org.dev.tequilacluster.models.bottling.TaxLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-22/RB-302,303,306: marbetes por folio único, disponibilidad y reconciliación contra unidades. */
public interface TaxLabelRepository extends JpaRepository<TaxLabel, UUID> {

    Optional<TaxLabel> findByFolio(String folio);

    List<TaxLabel> findByStatus(String status);

    List<TaxLabel> findByBottlingBatchIdAndStatus(UUID bottlingBatchId, String status);

    long countByBottlingBatchIdAndStatus(UUID bottlingBatchId, String status);

    boolean existsByFolio(String folio);
}
