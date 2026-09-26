package org.dev.tequilacluster.services.traceability;

import org.springframework.stereotype.Service;

/**
 * TODO(dev-b): implement backward/forward traceability — FR-37, FR-38, FR-43, RB-501/RB-502.
 * Define your own response DTOs in {@code dtos.traceability} and a controller (e.g.
 * {@code GET /api/v1/traceability/{traceabilityCode}}).
 *
 * <ul>
 *   <li>FR-37/RB-501 (backward): from a traceability_code or a bottled_unit unit_code, walk
 *       bottled_unit → bottling_batch → (batch_lineage) → distillation_batch →
 *       (batch_lineage) → jima_batch(es) → agave_field / supplier / authorized_production_area.
 *       {@code BatchLineageRepository} gives you the parent/child links; recurse or iterate
 *       since a distillation batch can have more than one harvest batch as parent (RB-201).</li>
 *   <li>FR-38/RB-502 (forward): given an agave_field, supplier or batch, list every batch
 *       derived from it (inverse traversal of the same batch_lineage graph).</li>
 *   <li>FR-43: full history of a batch from its traceability_code — combine
 *       {@code BatchTransitionHistoryRepository.findByBatch_IdOrderByChangedAtDesc} with the
 *       stage detail rows (jima_batch/distillation_batch/bottling_batch) for that batch.</li>
 *   <li>NFR-14: a full backward traceability query must respond in &le; 5s — avoid N+1 queries
 *       when walking batch_lineage for large chains; consider a recursive native query if the
 *       naive repository approach is too slow once you have real data volume.</li>
 * </ul>
 */
@Service
public class TraceabilityQueryService {
}
