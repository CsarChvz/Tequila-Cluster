package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchLineageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** RB-501/502: genealogía de lotes para trazabilidad hacia atrás (parent) y hacia adelante (child). */
public interface BatchLineageRepository extends JpaRepository<BatchLineage, BatchLineageId> {

    /** FR-37: trazabilidad hacia atrás — lotes padre que originaron el lote hijo. */
    List<BatchLineage> findByChildBatch_Id(UUID childBatchId);

    @Query("SELECT bl FROM BatchLineage bl WHERE bl.childBatch.id = :childBatchId")
    List<BatchLineage> findByChildBatchId(@Param("childBatchId") UUID childBatchId);

    /** FR-38: trazabilidad hacia adelante — lotes hijo derivados de un lote padre. */
    List<BatchLineage> findByParentBatch_Id(UUID parentBatchId);

    @Query("SELECT bl FROM BatchLineage bl WHERE bl.parentBatch.id = :parentBatchId")
    List<BatchLineage> findByParentBatchId(@Param("parentBatchId") UUID parentBatchId);

    @Query("SELECT bl FROM BatchLineage bl JOIN FETCH bl.parentBatch p JOIN FETCH p.processStage JOIN FETCH bl.childBatch c JOIN FETCH c.processStage WHERE bl.childBatch.id IN :childBatchIds")
    List<BatchLineage> findByChildBatch_IdIn(@Param("childBatchIds") java.util.Collection<UUID> childBatchIds);

    @Query("SELECT bl FROM BatchLineage bl JOIN FETCH bl.parentBatch p JOIN FETCH p.processStage JOIN FETCH bl.childBatch c JOIN FETCH c.processStage WHERE bl.parentBatch.id IN :parentBatchIds")
    List<BatchLineage> findByParentBatch_IdIn(@Param("parentBatchIds") java.util.Collection<UUID> parentBatchIds);
}
