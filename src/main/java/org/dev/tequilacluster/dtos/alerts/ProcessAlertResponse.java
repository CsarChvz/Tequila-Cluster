package org.dev.tequilacluster.dtos.alerts;

import org.dev.tequilacluster.models.shared.enums.AlertSeverity;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-44: Process alert response DTO.
 */
public record ProcessAlertResponse(
        UUID id,
        UUID batchId,
        String batchTraceabilityCode,
        String batchStageCode,
        UUID shipmentId,
        String shipmentNumber,
        String alertType,
        AlertSeverity severity,
        String message,
        Instant detectedAt,
        Instant resolvedAt,
        UUID resolvedByUserId,
        String resolvedByUsername
) {}
