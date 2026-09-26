package org.dev.tequilacluster.services.inventory;

import org.springframework.stereotype.Service;

/**
 * TODO(dev-b): implement Inventory — FR-35/FR-36, RB-506. Define your own DTOs in
 * {@code dtos.inventory} and a controller once designed.
 *
 * <ul>
 *   <li>FR-35: record a movement per bottling_batch + inventory_location, type INITIAL /
 *       PRODUCTION_IN / RETURN_IN / SHIPMENT_OUT / LOSS_OUT / RECALL_OUT / ADJUSTMENT, with
 *       reason and reference (reference_type/reference_id, e.g. pointing at the shipment or
 *       recall that caused it).</li>
 *   <li>FR-36/RB-506: current stock per (bottling_batch, location) = sum of quantity_change_units
 *       for that pair; expose an endpoint that computes and reports discrepancies (define what
 *       "discrepancy" means for your reconciliation — e.g. vs. expected units_bottled minus
 *       shipped/lost — and document it).</li>
 * </ul>
 */
@Service
public class InventoryMovementService {
}
