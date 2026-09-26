package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-37/38/39/43: entidad central de trazabilidad. */
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    Optional<Batch> findByTraceabilityCode(String traceabilityCode);

    boolean existsByTraceabilityCode(String traceabilityCode);

    List<Batch> findByProcessStage_CodeAndStatus(String stageCode, BatchStatus status);

    List<Batch> findByProcessStage_Code(String stageCode);

    List<Batch> findByStatus(BatchStatus status);

    List<Batch> findByCreatedBy_Id(UUID createdByUserId);
}
