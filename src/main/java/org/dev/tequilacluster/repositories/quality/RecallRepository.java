package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.Recall;
import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-34: retiro de producto (parcial/total) desde un batch origen, opcionalmente ligado a una no conformidad. */
public interface RecallRepository extends JpaRepository<Recall, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Recall r WHERE r.id = :id")
    Optional<Recall> findByIdForUpdate(@Param("id") UUID id);

    List<Recall> findBySourceBatch_Id(UUID sourceBatchId);

    List<Recall> findByNonConformity_Id(UUID nonConformityId);

    List<Recall> findByStatus(RecallStatus status);
}
