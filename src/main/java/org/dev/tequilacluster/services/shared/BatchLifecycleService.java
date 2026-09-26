package org.dev.tequilacluster.services.shared;

import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchTransitionHistory;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.BatchTransitionHistoryRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic batch state machine shared by every stage (harvest/distillation/bottling): RB-001,
 * RB-002, RB-003, RB-503, FR-39 to FR-41. Stage-specific services call this instead of touching
 * {@code batch.status} directly, so the transition history and the read-only-once-COMPLETED
 * rule (NFR-08) are always honored.
 */
@Service
public class BatchLifecycleService {

    private final BatchRepository batchRepository;
    private final BatchTransitionHistoryRepository transitionHistoryRepository;
    private final ProcessAlertRepository processAlertRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    public BatchLifecycleService(
            BatchRepository batchRepository,
            BatchTransitionHistoryRepository transitionHistoryRepository,
            ProcessAlertRepository processAlertRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService
    ) {
        this.batchRepository = batchRepository;
        this.transitionHistoryRepository = transitionHistoryRepository;
        this.processAlertRepository = processAlertRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    /** Moves a DRAFT batch to IN_PROGRESS once its stage-specific detail row has been captured. */
    @Transactional
    public void start(UUID batchId, UUID changedBy) {
        transition(batchId, BatchStatus.IN_PROGRESS, changedBy, "Stage data captured");
    }

    /**
     * RB-001: rejects completion while any CRITICAL alert on this batch is unresolved. Stage
     * services must run their own field-level validations (RB-202, RB-203, RB-305, ...) before
     * calling this — this method only enforces the alert gate and read-only transition.
     */
    @Transactional
    public void complete(UUID batchId, UUID changedBy) {
        boolean hasOpenCriticalAlert = !processAlertRepository
                .findByBatch_IdAndSeverityAndResolvedAtIsNull(batchId, AlertSeverity.CRITICAL)
                .isEmpty();
        if (hasOpenCriticalAlert) {
            throw new BusinessRuleViolationException("RB-001",
                    "Batch cannot be completed while a CRITICAL alert is unresolved: " + batchId);
        }
        transition(batchId, BatchStatus.COMPLETED, changedBy, "All stage validations passed");
    }

    /** RB-503 / NFR-09: batches are never deleted, only cancelled with a mandatory reason. */
    @Transactional
    public void cancel(UUID batchId, UUID changedBy, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleViolationException("RB-503", "Cancelling a batch requires a reason");
        }
        Batch batch = getOrThrow(batchId);
        assertNotCompleted(batch);

        BatchStatus from = batch.getStatus();
        batch.setStatus(BatchStatus.CANCELLED);
        batch.setCancelledAt(Instant.now());
        batch.setCancellationReason(reason);
        batchRepository.save(batch);

        recordTransition(batch, from, BatchStatus.CANCELLED, changedBy, reason);
        auditLogService.record(changedBy, "CANCEL", "batch", batchId, null, null);
    }

    private void transition(UUID batchId, BatchStatus to, UUID changedBy, String reason) {
        Batch batch = getOrThrow(batchId);
        assertNotCompleted(batch);

        BatchStatus from = batch.getStatus();
        batch.setStatus(to);
        if (to == BatchStatus.COMPLETED) {
            batch.setCompletedAt(Instant.now());
        }
        batchRepository.save(batch);

        recordTransition(batch, from, to, changedBy, reason);
        auditLogService.record(changedBy, "TRANSITION", "batch", batchId, null, null);
    }

    private void assertNotCompleted(Batch batch) {
        // NFR-08 / RB-002: once COMPLETED, a batch's records are read-only for every role.
        if (batch.getStatus() == BatchStatus.COMPLETED) {
            throw new BusinessRuleViolationException("RB-002", "Batch is COMPLETED and read-only: " + batch.getId());
        }
    }

    private void recordTransition(Batch batch, BatchStatus from, BatchStatus to, UUID changedBy, String reason) {
        BatchTransitionHistory history = new BatchTransitionHistory();
        history.setBatch(batch);
        history.setFromStage(batch.getProcessStage());
        history.setToStage(batch.getProcessStage());
        history.setFromStatus(from == null ? null : from.name());
        history.setToStatus(to.name());
        if (changedBy != null) {
            AppUser userRef = appUserRepository.getReferenceById(changedBy);
            history.setChangedBy(userRef);
        }
        history.setReason(reason);
        history.setChangedAt(Instant.now());
        transitionHistoryRepository.save(history);
    }

    private Batch getOrThrow(UUID batchId) {
        return batchRepository.findById(batchId).orElseThrow(() -> NotFoundException.of("Batch", batchId));
    }
}
