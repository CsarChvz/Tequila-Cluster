package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.BatchTransitionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-41/43: historial append-only de transiciones de etapa/status de un lote. */
public interface BatchTransitionHistoryRepository extends JpaRepository<BatchTransitionHistory, UUID> {

    List<BatchTransitionHistory> findByBatch_IdOrderByChangedAtDesc(UUID batchId);

    List<BatchTransitionHistory> findByChangedBy_Id(UUID changedByUserId);
}
