package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.AuthorizedProductionArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RB-101/FR-07: validar que el área autorizada (DO) esté activa y vigente. */
public interface AuthorizedProductionAreaRepository extends JpaRepository<AuthorizedProductionArea, UUID> {

    Optional<AuthorizedProductionArea> findByCode(String code);

    Optional<AuthorizedProductionArea> findByCodeAndActiveTrue(String code);

    List<AuthorizedProductionArea> findByActiveTrue();

    boolean existsByCode(String code);
}
