package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findByName(String name);

    List<Brand> findByActiveTrue();
}
