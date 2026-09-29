package org.dev.tequilacluster.dtos.traceability;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-43: Detailed batch transition history entry.
 */
public record BatchTransitionHistoryDto(
        UUID id,
        String fromStageCode,
        String fromStageName,
        String toStageCode,
        String toStageName,
        String fromStatus,
        String toStatus,
        UUID changedByUserId,
        String changedByUsername,
        String reason,
        Instant changedAt
) {}
