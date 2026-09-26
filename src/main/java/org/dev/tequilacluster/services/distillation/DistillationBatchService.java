package org.dev.tequilacluster.services.distillation;

import org.dev.tequilacluster.dtos.distillation.DistillationBatchCreateRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * TODO(dev-a): implement the Distillation stage — FR-12 to FR-19, RB-201 to RB-207.
 * Follow the shape of {@code org.dev.tequilacluster.services.harvest.JimaBatchService}
 * (the reference implementation): validate → create {@code Batch} (stage
 * {@link org.dev.tequilacluster.utils.shared.ProcessStageCodes#DISTILLATION}) + detail row →
 * side effects (alerts, batch_lineage) → {@code BatchLifecycleService.start}. Use
 * {@code services.shared.AlertService} for CRITICAL/WARNING alerts and
 * {@code services.shared.BatchLifecycleService} for status transitions — do not touch
 * {@code batch.status} directly. See TODO_TequilaCluster.md for the full checklist.
 */
@Service
public class DistillationBatchService {

    // TODO(dev-a) FR-12/RB-201/RB-001: only from harvest batches with status COMPLETED.
    // TODO(dev-a) FR-13/RB-201: link via batch_lineage (BatchLineageRepository), one row per
    //             sourceHarvestBatches item (quantity_used, unit).
    // TODO(dev-a) FR-14/FR-15/RB-202: heads+hearts+tails <= total_distilled_volume_l, else
    //             BusinessRuleViolationException("RB-202", ...).
    // TODO(dev-a) FR-16/RB-203: alcohol_content_pct outside validation_rule range (stage
    //             DISTILLATION, parameter e.g. "ALCOHOL_CONTENT_PCT") -> AlertService
    //             .raiseForBatch(..., CRITICAL, ...); batch cannot COMPLETE while open (already
    //             enforced generically by BatchLifecycleService.complete).
    // TODO(dev-a) FR-17/RB-103/RB-204: compare actual_yield_l vs estimated_yield_l_snapshot
    //             (copy the harvest batch's estimated_yield_l when creating this batch).
    // TODO(dev-a) FR-18/RB-207: cooking_temperature_c / fermentation_ph outside validation_rule
    //             range -> alert (severity: your judgment, ERS doesn't specify CRITICAL here).
    // TODO(dev-a) FR-19/RB-205/RB-206: maturation_required, maturation_start_date,
    //             required_maturation_days; compute ready_for_bottling_at once elapsed (or
    //             immediately if !maturationRequired and batch is COMPLETED).
    public UUID create(DistillationBatchCreateRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Distillation stage not implemented yet — see TODO_TequilaCluster.md (Dev A)");
    }
}
