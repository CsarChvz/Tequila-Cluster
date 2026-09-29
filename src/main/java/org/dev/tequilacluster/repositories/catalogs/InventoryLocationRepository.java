package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, UUID> {

    Optional<InventoryLocation> findByCode(String code);

    List<InventoryLocation> findByActiveTrue();
}
