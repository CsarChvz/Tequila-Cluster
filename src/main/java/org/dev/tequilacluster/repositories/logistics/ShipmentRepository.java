package org.dev.tequilacluster.repositories.logistics;

import jakarta.persistence.LockModeType;
import org.dev.tequilacluster.models.logistics.Shipment;
import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-28/31/32: embarque de producto terminado, estado y control de llegada estimada. */
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Shipment s WHERE s.id = :id")
    Optional<Shipment> findByIdForUpdate(@Param("id") UUID id);

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    /** FR-31: embarques cuya llegada estimada ya pasó y siguen sin entregar (candidatos a alerta por retraso). */
    List<Shipment> findByStatusAndEstimatedArrivalAtBefore(ShipmentStatus status, Instant before);

    List<Shipment> findByCarrier_Id(UUID carrierId);

    boolean existsByShipmentNumber(String shipmentNumber);
}
