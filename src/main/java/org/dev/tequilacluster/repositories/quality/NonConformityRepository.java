package org.dev.tequilacluster.repositories.quality;

import jakarta.persistence.LockModeType;
import org.dev.tequilacluster.models.quality.NonConformity;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-33: incidencias de calidad sobre un batch (opcionalmente una unidad embotellada). */
public interface NonConformityRepository extends JpaRepository<NonConformity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT nc FROM NonConformity nc WHERE nc.id = :id")
    Optional<NonConformity> findByIdForUpdate(@Param("id") UUID id);

    List<NonConformity> findByBatch_Id(UUID batchId);

    List<NonConformity> findByBottledUnit_Id(UUID bottledUnitId);

    List<NonConformity> findByStatus(NonConformityStatus status);

    List<NonConformity> findBySeverity(NonConformitySeverity severity);
}
