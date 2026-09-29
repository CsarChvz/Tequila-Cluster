package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentTypeRepository extends JpaRepository<ShipmentType, UUID> {

    Optional<ShipmentType> findByCode(String code);

    List<ShipmentType> findByActiveTrue();
}
