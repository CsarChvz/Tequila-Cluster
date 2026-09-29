package org.dev.tequilacluster.services.shared;

import org.dev.tequilacluster.dtos.shared.AuditLogResponse;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.AuditLog;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FR-42 / NFR-10: append-only audit log shared by every module. Never expose an update/delete
 * for this entity — only {@link #record} (insert).
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    public void record(UUID userId, String action, String entityType, UUID entityId, String beforeData, String afterData) {
        AuditLog entry = new AuditLog();
        if (userId != null) {
            AppUser userRef = appUserRepository.getReferenceById(userId);
            entry.setUser(userRef);
        }
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setBeforeData(beforeData);
        entry.setAfterData(afterData);
        entry.setOccurredAt(Instant.now());
        auditLogRepository.save(entry);
        log.debug("Audit log recorded: action={} entityType={} entityId={} user={}", action, entityType, entityId, userId);
    }

    /** FR-42: recent audit log entries for the auditor dashboard (Administrator/Auditor only). */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listRecent(int limit) {
        return auditLogRepository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, Math.max(1, limit))).stream()
                .map(entry -> new AuditLogResponse(
                        entry.getId(),
                        entry.getUser() != null ? entry.getUser().getId() : null,
                        entry.getUser() != null ? entry.getUser().getUsername() : null,
                        entry.getAction(),
                        entry.getEntityType(),
                        entry.getEntityId(),
                        entry.getBeforeData(),
                        entry.getAfterData(),
                        entry.getIpAddress(),
                        entry.getOccurredAt()
                ))
                .toList();
    }
}
