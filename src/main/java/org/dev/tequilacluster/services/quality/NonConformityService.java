package org.dev.tequilacluster.services.quality;

import org.springframework.stereotype.Service;

/**
 * TODO(dev-b): implement Quality — FR-33/FR-34, RB-407. Define your own DTOs in
 * {@code dtos.quality} and a controller once designed. Consider splitting recall handling into
 * its own {@code RecallService} if this grows too large — your call.
 *
 * <ul>
 *   <li>FR-33: non-conformity on a batch, optionally on a bottled_unit; title, description,
 *       severity (LOW/MEDIUM/HIGH/CRITICAL); status OPEN → INVESTIGATING → RESOLVED → CLOSED.</li>
 *   <li>FR-34/RB-407: a recall (PARTIAL/COMPLETE) from a source batch, optionally linked to a
 *       non_conformity; list affected bottled units via recall_unit, set each to RECALLED
 *       (BottledUnit.status), and control recall status OPEN → IN_PROGRESS → COMPLETED/CANCELLED.</li>
 * </ul>
 */
@Service
public class NonConformityService {
}
