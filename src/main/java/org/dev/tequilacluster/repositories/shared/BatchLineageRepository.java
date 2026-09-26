package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchLineageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** RB-501/502: genealogía de lotes para trazabilidad hacia atrás (parent) y hacia adelante (child). */
public interface BatchLineageRepository extends JpaRepository<BatchLineage, BatchLineageId> {

    /** FR-37: trazabilidad hacia atrás — lotes padre que originaron el lote hijo. */
    List<BatchLineage> findByChildBatchId(UUID childBatchId);

    /** FR-38: trazabilidad hacia adelante — lotes hijo derivados de un lote padre. */
    List<BatchLineage> findByParentBatchId(UUID parentBatchId);
}
