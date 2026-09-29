package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.RecallUnit;
import org.dev.tequilacluster.models.quality.RecallUnitId;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** FR-34: unidades embotelladas individuales afectadas por un recall parcial. */
public interface RecallUnitRepository extends JpaRepository<RecallUnit, RecallUnitId> {

    List<RecallUnit> findByRecall_Id(UUID recallId);

    long countByRecall_Id(UUID recallId);

    List<RecallUnit> findByBottledUnit_Id(UUID bottledUnitId);

    @Query("SELECT COUNT(ru) > 0 FROM RecallUnit ru WHERE ru.recall.id = :recallId AND ru.bottledUnit.bottlingBatch.id = :bottlingBatchId")
    boolean existsByRecallIdAndBottlingBatchId(@Param("recallId") UUID recallId, @Param("bottlingBatchId") UUID bottlingBatchId);

    @Query("SELECT COUNT(ru) FROM RecallUnit ru WHERE ru.recall.id = :recallId AND ru.bottledUnit.bottlingBatch.id = :bottlingBatchId")
    long countByRecallIdAndBottlingBatchId(@Param("recallId") UUID recallId, @Param("bottlingBatchId") UUID bottlingBatchId);
}
