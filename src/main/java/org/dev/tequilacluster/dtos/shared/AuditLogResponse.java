package org.dev.tequilacluster.dtos.shared;

import java.time.Instant;
import java.util.UUID;

/** FR-42: audit log entry for the auditor's activity dashboard. */
public record AuditLogResponse(
        Long id,
        UUID userId,
        String username,
        String action,
        String entityType,
        UUID entityId,
        String beforeData,
        String afterData,
        String ipAddress,
        Instant occurredAt
) {
}
