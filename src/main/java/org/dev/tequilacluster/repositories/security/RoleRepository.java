package org.dev.tequilacluster.repositories.security;

import org.dev.tequilacluster.models.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    /** FR-02: catálogo de roles activos disponibles para asignación (Administrator, Operators, Auditor). */
    List<Role> findByActiveTrue();

    boolean existsByCode(String code);
}
