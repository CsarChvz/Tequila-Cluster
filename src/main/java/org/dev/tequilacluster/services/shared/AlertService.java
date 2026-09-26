package org.dev.tequilacluster.services.shared;

import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * FR-44: alerts linked to a batch or a shipment. Every stage service calls
 * {@link #raiseForBatch} instead of writing to {@code process_alert} directly, so the panel and
 * the RB-001 "no open CRITICAL alert" gate ({@code BatchLifecycleService.complete}) stay
 * consistent.
 */
@Service
public class AlertService {

    private final ProcessAlertRepository processAlertRepository;
    private final BatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;

    public AlertService(
            ProcessAlertRepository processAlertRepository,
            BatchRepository batchRepository,
            ShipmentRepository shipmentRepository,
            AppUserRepository appUserRepository
    ) {
        this.processAlertRepository = processAlertRepository;
        this.batchRepository = batchRepository;
        this.shipmentRepository = shipmentRepository;
        this.appUserRepository = appUserRepository;
    }

    public ProcessAlert raiseForBatch(UUID batchId, String alertType, AlertSeverity severity, String message) {
        Batch batchRef = batchRepository.getReferenceById(batchId);
        ProcessAlert alert = new ProcessAlert();
        alert.setBatch(batchRef);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setDetectedAt(Instant.now());
        return processAlertRepository.save(alert);
    }

    public ProcessAlert raiseForShipment(UUID shipmentId, String alertType, AlertSeverity severity, String message) {
        ProcessAlert alert = new ProcessAlert();
        alert.setShipment(shipmentRepository.getReferenceById(shipmentId));
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setDetectedAt(Instant.now());
        return processAlertRepository.save(alert);
    }

    public void resolve(UUID alertId, UUID resolvedByUserId) {
        ProcessAlert alert = processAlertRepository.findById(alertId)
                .orElseThrow(() -> org.dev.tequilacluster.exceptions.NotFoundException.of("ProcessAlert", alertId));
        alert.setResolvedAt(Instant.now());
        AppUser resolver = appUserRepository.getReferenceById(resolvedByUserId);
        alert.setResolvedBy(resolver);
        processAlertRepository.save(alert);
    }
}
