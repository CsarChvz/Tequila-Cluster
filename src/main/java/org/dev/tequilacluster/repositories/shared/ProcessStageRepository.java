package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.ProcessStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** FR-39: catálogo de etapas ordenadas (sort_order) para representar el flujo del proceso. */
public interface ProcessStageRepository extends JpaRepository<ProcessStage, String> {

    List<ProcessStage> findAllByOrderBySortOrderAsc();
}
