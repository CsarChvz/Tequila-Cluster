package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.Recall;
import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-34: retiro de producto (parcial/total) desde un batch origen, opcionalmente ligado a una no conformidad. */
public interface RecallRepository extends JpaRepository<Recall, UUID> {

    List<Recall> findBySourceBatch_Id(UUID sourceBatchId);

    List<Recall> findByNonConformity_Id(UUID nonConformityId);

    List<Recall> findByStatus(RecallStatus status);
}
