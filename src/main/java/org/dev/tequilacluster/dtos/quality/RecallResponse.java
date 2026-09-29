package org.dev.tequilacluster.dtos.quality;

import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import org.dev.tequilacluster.models.quality.enums.RecallType;

import java.time.Instant;
import java.util.UUID;

/** FR-34: Response DTO with recall details and affected unit metrics. */
public record RecallResponse(
        UUID id,
        UUID sourceBatchId,
        String sourceBatchTraceabilityCode,
        String sourceBatchStageCode,
        UUID nonConformityId,
        String nonConformityTitle,
        RecallType recallType,
        String reason,
        RecallStatus status,
        UUID startedById,
        String startedByUsername,
        Instant startedAt,
        Instant completedAt,
        int affectedUnitsCount
) {
}
