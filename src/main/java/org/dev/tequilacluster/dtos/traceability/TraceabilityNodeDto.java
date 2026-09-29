package org.dev.tequilacluster.dtos.traceability;

import org.dev.tequilacluster.models.shared.enums.BatchStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Node representation in batch traceability graph.
 */
public record TraceabilityNodeDto(
        UUID batchId,
        String traceabilityCode,
        String stageCode,
        String stageName,
        BatchStatus status,
        BigDecimal volume,
        Instant createdAt,
        Instant completedAt,
        Instant cancelledAt
) {}
