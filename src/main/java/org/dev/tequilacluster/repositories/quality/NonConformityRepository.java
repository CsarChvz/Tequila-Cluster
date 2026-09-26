package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.NonConformity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-33: incidencias de calidad sobre un batch (opcionalmente una unidad embotellada). */
public interface NonConformityRepository extends JpaRepository<NonConformity, UUID> {

    List<NonConformity> findByBatchId(UUID batchId);

    List<NonConformity> findByBottledUnitId(UUID bottledUnitId);

    List<NonConformity> findByStatus(String status);

    List<NonConformity> findBySeverity(String severity);
}
