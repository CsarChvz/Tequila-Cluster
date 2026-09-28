package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.NonConformity;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-33: incidencias de calidad sobre un batch (opcionalmente una unidad embotellada). */
public interface NonConformityRepository extends JpaRepository<NonConformity, UUID> {

    List<NonConformity> findByBatch_Id(UUID batchId);

    List<NonConformity> findByBottledUnit_Id(UUID bottledUnitId);

    List<NonConformity> findByStatus(NonConformityStatus status);

    List<NonConformity> findBySeverity(NonConformitySeverity severity);
}
