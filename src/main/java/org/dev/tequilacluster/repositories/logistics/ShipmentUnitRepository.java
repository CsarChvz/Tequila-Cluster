package org.dev.tequilacluster.repositories.logistics;

import org.dev.tequilacluster.models.logistics.ShipmentUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la asociación individual de botellas a embarques (FR-32).
 */
public interface ShipmentUnitRepository extends JpaRepository<ShipmentUnit, UUID> {

    List<ShipmentUnit> findByShipment_IdAndReleasedAtIsNull(UUID shipmentId);

    List<ShipmentUnit> findByShipment_Id(UUID shipmentId);

    Optional<ShipmentUnit> findByBottledUnit_IdAndReleasedAtIsNull(UUID bottledUnitId);

    List<ShipmentUnit> findByBottledUnit_IdInAndReleasedAtIsNull(Collection<UUID> bottledUnitIds);

    List<ShipmentUnit> findByBottledUnit_IdOrderByAssignedAtDesc(UUID bottledUnitId);

    boolean existsByBottledUnit_IdAndReleasedAtIsNull(UUID bottledUnitId);
}
