package org.dev.tequilacluster.repositories.bottling;

import org.dev.tequilacluster.models.bottling.TaxLabel;
import org.dev.tequilacluster.models.bottling.enums.TaxLabelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-22/RB-302,303,306: marbetes por folio único, disponibilidad y reconciliación contra unidades. */
public interface TaxLabelRepository extends JpaRepository<TaxLabel, UUID> {

    Optional<TaxLabel> findByFolio(String folio);

    List<TaxLabel> findByStatus(TaxLabelStatus status);

    @Query("SELECT t FROM TaxLabel t WHERE CAST(t.status AS string) = :status")
    List<TaxLabel> findByStatus(@Param("status") String status);

    List<TaxLabel> findByBottlingBatch_IdAndStatus(UUID bottlingBatchId, TaxLabelStatus status);

    @Query("SELECT t FROM TaxLabel t WHERE t.bottlingBatch.id = :bottlingBatchId AND CAST(t.status AS string) = :status")
    List<TaxLabel> findByBottlingBatchIdAndStatus(@Param("bottlingBatchId") UUID bottlingBatchId, @Param("status") String status);

    long countByBottlingBatch_IdAndStatus(UUID bottlingBatchId, TaxLabelStatus status);

    @Query("SELECT COUNT(t) FROM TaxLabel t WHERE t.bottlingBatch.id = :bottlingBatchId AND CAST(t.status AS string) = :status")
    long countByBottlingBatchIdAndStatus(@Param("bottlingBatchId") UUID bottlingBatchId, @Param("status") String status);

    boolean existsByFolio(String folio);
}
