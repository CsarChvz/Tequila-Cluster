package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RB-102/FR-08: validar que el proveedor esté registrado y activo antes de crear un harvest batch. */
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    Optional<Supplier> findBySupplierCodeAndActiveTrue(String supplierCode);

    List<Supplier> findByActiveTrue();

    boolean existsBySupplierCode(String supplierCode);
}
