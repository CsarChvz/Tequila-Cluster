package org.dev.tequilacluster.repositories.logistics;

import org.dev.tequilacluster.models.logistics.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-28/31/32: embarque de producto terminado, estado y control de llegada estimada. */
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    List<Shipment> findByStatus(String status);

    /** FR-31: embarques cuya llegada estimada ya pasó y siguen sin entregar (candidatos a alerta por retraso). */
    List<Shipment> findByStatusAndEstimatedArrivalAtBefore(String status, OffsetDateTime before);

    List<Shipment> findByCarrierId(UUID carrierId);

    boolean existsByShipmentNumber(String shipmentNumber);
}
