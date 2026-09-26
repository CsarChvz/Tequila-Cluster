package org.dev.tequilacluster.repositories.harvest;

import org.dev.tequilacluster.models.harvest.TransportPermit;
import org.dev.tequilacluster.models.harvest.enums.TransportPermitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-10: se genera automáticamente un permiso por cada harvest batch (relación 1:1). */
public interface TransportPermitRepository extends JpaRepository<TransportPermit, UUID> {

    Optional<TransportPermit> findByPermitNumber(String permitNumber);

    Optional<TransportPermit> findByJimaBatch_BatchId(UUID jimaBatchId);

    List<TransportPermit> findByStatus(TransportPermitStatus status);

    boolean existsByPermitNumber(String permitNumber);
}
