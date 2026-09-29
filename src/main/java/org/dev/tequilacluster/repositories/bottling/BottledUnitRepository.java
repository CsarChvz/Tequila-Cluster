package org.dev.tequilacluster.repositories.bottling;

import jakarta.persistence.LockModeType;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-23/37: unidad física trazable por QR/barcode único, ligada a batch y marbete. */
public interface BottledUnitRepository extends JpaRepository<BottledUnit, UUID> {

    Optional<BottledUnit> findByUnitCode(String unitCode);

    Optional<BottledUnit> findByTaxLabel_Id(UUID taxLabelId);

    List<BottledUnit> findByBottlingBatch_Id(UUID bottlingBatchId);

    List<BottledUnit> findByBottlingBatch_IdAndStatus(UUID bottlingBatchId, BottledUnitStatus status);

    long countByBottlingBatch_IdAndStatus(UUID bottlingBatchId, BottledUnitStatus status);

    @Query("SELECT u FROM BottledUnit u WHERE u.bottlingBatch.id = :bottlingBatchId AND CAST(u.status AS string) = :status")
    List<BottledUnit> findByBottlingBatchIdAndStatus(@Param("bottlingBatchId") UUID bottlingBatchId, @Param("status") String status);

    @Query("SELECT COUNT(u) FROM BottledUnit u WHERE u.bottlingBatch.id = :bottlingBatchId AND CAST(u.status AS string) = :status")
    long countByBottlingBatchIdAndStatus(@Param("bottlingBatchId") UUID bottlingBatchId, @Param("status") String status);

    boolean existsByUnitCode(String unitCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM BottledUnit u WHERE u.bottlingBatch.id = :bottlingBatchId AND u.status = :status ORDER BY u.unitCode ASC")
    List<BottledUnit> findAvailableForReservation(
            @Param("bottlingBatchId") UUID bottlingBatchId,
            @Param("status") BottledUnitStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM BottledUnit u WHERE u.id IN :ids ORDER BY u.id ASC")
    List<BottledUnit> findAllByIdInForUpdate(@Param("ids") Collection<UUID> ids);

    @Query("SELECT u.id FROM BottledUnit u WHERE u.bottlingBatch.id IN :bottlingBatchIds ORDER BY u.id ASC")
    List<UUID> findIdsByBottlingBatchIdIn(@Param("bottlingBatchIds") Collection<UUID> bottlingBatchIds);

    @Query("SELECT DISTINCT u.bottlingBatch.id FROM BottledUnit u WHERE u.id IN :ids")
    List<UUID> findDistinctBottlingBatchIdsByUnitIds(@Param("ids") Collection<UUID> ids);

    @Query("SELECT u.status, COUNT(u) FROM BottledUnit u WHERE u.bottlingBatch.id = :bottlingBatchId GROUP BY u.status")
    List<Object[]> countGroupByStatusForBatch(@Param("bottlingBatchId") UUID bottlingBatchId);

    long countByBottlingBatch_Id(UUID bottlingBatchId);
}
