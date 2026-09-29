package org.dev.tequilacluster.repositories.quality;

import org.dev.tequilacluster.models.quality.RecallUnit;
import org.dev.tequilacluster.models.quality.RecallUnitId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-34: unidades embotelladas individuales afectadas por un recall parcial. */
public interface RecallUnitRepository extends JpaRepository<RecallUnit, RecallUnitId> {

    List<RecallUnit> findByRecall_Id(UUID recallId);

    long countByRecall_Id(UUID recallId);

    List<RecallUnit> findByBottledUnit_Id(UUID bottledUnitId);
}
