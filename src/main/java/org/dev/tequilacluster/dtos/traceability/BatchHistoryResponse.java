package org.dev.tequilacluster.dtos.traceability;

import org.dev.tequilacluster.dtos.alerts.ProcessAlertResponse;
import org.dev.tequilacluster.dtos.quality.NonConformityResponse;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FR-43: Full lifecycle history response for a batch.
 */
public record BatchHistoryResponse(
        UUID batchId,
        String traceabilityCode,
        String stageCode,
        String stageName,
        BatchStatus status,
        UUID createdByUserId,
        String createdByUsername,
        Instant createdAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancellationReason,
        Object stageDetail,
        List<BatchTransitionHistoryDto> transitions,
        List<ProcessAlertResponse> alerts,
        List<NonConformityResponse> nonConformities
) {}
