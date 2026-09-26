package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-44/RB-001,203: alertas ligadas a batch o embarque, con panel de resolución por rol. */
public interface ProcessAlertRepository extends JpaRepository<ProcessAlert, UUID> {

    /** Alertas abiertas (no resueltas) para el panel visible según rol. */
    List<ProcessAlert> findByResolvedAtIsNull();

    List<ProcessAlert> findByBatch_Id(UUID batchId);

    List<ProcessAlert> findByShipment_Id(UUID shipmentId);

    /** RB-001/RB-203: alertas CRITICAL sin resolver bloquean el paso de un batch a COMPLETED. */
    List<ProcessAlert> findByBatch_IdAndSeverityAndResolvedAtIsNull(UUID batchId, AlertSeverity severity);

    List<ProcessAlert> findBySeverityAndResolvedAtIsNull(AlertSeverity severity);
}
