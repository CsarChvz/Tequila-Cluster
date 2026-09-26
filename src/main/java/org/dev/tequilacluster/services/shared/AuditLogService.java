package org.dev.tequilacluster.services.shared;

import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.AuditLog;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-42 / NFR-10: append-only audit log shared by every module. Never expose an update/delete
 * for this entity — only {@link #record} (insert).
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    public void record(UUID userId, String action, String entityType, UUID entityId, String beforeData, String afterData) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            AppUser userRef = appUserRepository.getReferenceById(userId);
            log.setUser(userRef);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setOccurredAt(Instant.now());
        auditLogRepository.save(log);
    }
}
