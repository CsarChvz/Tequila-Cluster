package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-42: bitácora general de auditoría, append-only (NFR-10). */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, UUID entityId);

    List<AuditLog> findByUser_IdOrderByOccurredAtDesc(UUID userId);

    List<AuditLog> findByActionOrderByOccurredAtDesc(String action);
}
