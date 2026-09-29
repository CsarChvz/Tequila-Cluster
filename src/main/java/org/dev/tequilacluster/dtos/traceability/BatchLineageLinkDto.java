package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Directed link parent -> child in batch traceability graph.
 */
public record BatchLineageLinkDto(
        UUID parentBatchId,
        UUID childBatchId,
        BigDecimal quantityUsed,
        String unit,
        Instant linkedAt
) {}
