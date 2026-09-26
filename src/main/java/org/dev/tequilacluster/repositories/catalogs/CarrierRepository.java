package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarrierRepository extends JpaRepository<Carrier, UUID> {

    List<Carrier> findByActiveTrue();

    List<Carrier> findByNameContainingIgnoreCase(String name);
}
