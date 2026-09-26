package org.dev.tequilacluster.services.bottling;

import org.springframework.stereotype.Service;

/**
 * TODO(dev-a): implement the Bottling stage — FR-20 to FR-25, RB-301 to RB-307. Empty on
 * purpose: define your own request/response DTOs in {@code dtos.bottling} and the CRUD/
 * controller once you design the request shape — don't build on a guessed contract.
 * Follow {@code services.harvest.JimaBatchService} as the reference shape (validate → create
 * Batch + detail → side effects → BatchLifecycleService.start/complete).
 *
 * <ul>
 *   <li>FR-20/RB-301/RB-205/RB-001: only from a COMPLETED DistillationBatch whose
 *       ready_for_bottling_at has been reached AND required_maturation_days &gt;=
 *       tequila_category.minimum_maturation_days of the chosen category.</li>
 *   <li>FR-21/RB-301: brand, category, bottling_date, bottle_capacity_ml, total_volume_l,
 *       production_lot_number (unique), units_bottled, registered_losses_units.</li>
 *   <li>FR-22/RB-302/RB-303: block if assigned TaxLabel count &lt; units_bottled; each TaxLabel
 *       (status ASSIGNED) is used by exactly one BottledUnit (unique FK).</li>
 *   <li>FR-23/RB-304: generate a unique unit_code (QR/BARCODE) per BottledUnit linked to its
 *       BottlingBatch + TaxLabel.</li>
 *   <li>FR-24/RB-305/RB-307: every active LabelRequirement needs a BottlingLabelValue before
 *       the batch can COMPLETE (validate before calling BatchLifecycleService.complete).</li>
 *   <li>FR-25/RB-306: reconcile tax labels used vs units_bottled + registered_losses_units,
 *       alert (AlertService) if the discrepancy exceeds the validation_rule allowed deviation
 *       (default 2%).</li>
 * </ul>
 */
@Service
public class BottlingBatchService {
}
