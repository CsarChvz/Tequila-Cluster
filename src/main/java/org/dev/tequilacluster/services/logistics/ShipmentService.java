package org.dev.tequilacluster.services.logistics;

import org.springframework.stereotype.Service;

/**
 * TODO(dev-b): implement Shipping Logistics — FR-26 to FR-32, RB-401 to RB-406. Define your own
 * DTOs in {@code dtos.logistics} and a {@code ShipmentController} once designed.
 *
 * <ul>
 *   <li>FR-26/FR-27/RB-401: only bottling batches with status COMPLETED; each shipment_item's
 *       quantity_units &lt;= that bottling batch's remaining available units (available =
 *       units_bottled − already-shipped/reserved units, track via BottledUnit.status or a
 *       running total — your call, document the approach).</li>
 *   <li>FR-28/RB-403: shipment_number (unique), shipment_type, carrier, vehicle_license_plate,
 *       destination, departure_at, estimated_arrival_at.</li>
 *   <li>FR-29/RB-402: a shipment leaves PLANNED only once every ShipmentTypeRequiredDocument
 *       for its shipment_type has a matching, valid ShipmentDocument.</li>
 *   <li>FR-30/RB-406: a bottling batch may be split across several shipments (sum of units
 *       across all its shipment_item rows &lt;= available units).</li>
 *   <li>FR-31/RB-404: alert (AlertService.raiseForShipment) when actual/observed arrival
 *       exceeds estimated_arrival_at by more than the validation_rule allowed deviation
 *       (default 48h) — needs a scheduled check or a query run on demand, your call.</li>
 *   <li>FR-32/RB-405: PLANNED → IN_TRANSIT → DELIVERED/CANCELLED; confirming delivery sets
 *       delivered_at and every shipped BottledUnit to DELIVERED.</li>
 * </ul>
 */
@Service
public class ShipmentService {
}
