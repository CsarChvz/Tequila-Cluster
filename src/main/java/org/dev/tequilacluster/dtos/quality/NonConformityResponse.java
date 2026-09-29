package org.dev.tequilacluster.dtos.quality;

import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;

import java.time.Instant;
import java.util.UUID;

/** FR-33: Response DTO with non-conformity details and readable batch/unit references. */
public record NonConformityResponse(
        UUID id,
        UUID batchId,
        String batchTraceabilityCode,
        String batchStageCode,
        UUID bottledUnitId,
        String unitCode,
        String title,
        String description,
        NonConformitySeverity severity,
        NonConformityStatus status,
        UUID reportedById,
        String reportedByUsername,
        Instant reportedAt,
        Instant resolvedAt
) {
}
