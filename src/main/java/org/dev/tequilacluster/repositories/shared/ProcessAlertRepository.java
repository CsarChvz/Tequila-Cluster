package org.dev.tequilacluster.repositories.shared;

import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
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

    boolean existsByShipment_IdAndAlertTypeAndResolvedAtIsNull(UUID shipmentId, String alertType);

    List<ProcessAlert> findByShipment_IdAndAlertTypeAndResolvedAtIsNull(UUID shipmentId, String alertType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ProcessAlert a LEFT JOIN FETCH a.batch b LEFT JOIN FETCH b.processStage LEFT JOIN FETCH a.shipment LEFT JOIN FETCH a.resolvedBy WHERE a.id = :id")
    Optional<ProcessAlert> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT a FROM ProcessAlert a " +
            "LEFT JOIN FETCH a.batch b " +
            "LEFT JOIN FETCH b.processStage " +
            "LEFT JOIN FETCH a.shipment " +
            "LEFT JOIN FETCH a.resolvedBy " +
            "WHERE (:batchId IS NULL OR a.batch.id = :batchId) " +
            "AND (:shipmentId IS NULL OR a.shipment.id = :shipmentId) " +
            "AND (:severity IS NULL OR a.severity = :severity) " +
            "AND (:statusFilter = 'ALL' OR (:statusFilter = 'OPEN' AND a.resolvedAt IS NULL) OR (:statusFilter = 'RESOLVED' AND a.resolvedAt IS NOT NULL)) " +
            "ORDER BY a.detectedAt DESC")
    List<ProcessAlert> findWithDetailsFiltered(
            @Param("batchId") UUID batchId,
            @Param("shipmentId") UUID shipmentId,
            @Param("severity") AlertSeverity severity,
            @Param("statusFilter") String statusFilter
    );

    @Query("SELECT a FROM ProcessAlert a " +
            "LEFT JOIN FETCH a.batch b " +
            "LEFT JOIN FETCH b.processStage " +
            "LEFT JOIN FETCH a.shipment " +
            "LEFT JOIN FETCH a.resolvedBy " +
            "WHERE a.batch.id = :batchId " +
            "ORDER BY a.detectedAt DESC")
    List<ProcessAlert> findByBatchIdOrderByDetectedAtDescWithDetails(@Param("batchId") UUID batchId);
}
