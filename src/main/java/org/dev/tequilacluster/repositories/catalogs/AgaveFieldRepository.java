package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.AgaveField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-06/FR-38: campo de origen de agave; trazabilidad hacia adelante por campo. */
public interface AgaveFieldRepository extends JpaRepository<AgaveField, UUID> {

    Optional<AgaveField> findByFieldCode(String fieldCode);

    List<AgaveField> findByAuthorizedArea_Id(UUID authorizedAreaId);

    List<AgaveField> findByActiveTrue();

    boolean existsByFieldCode(String fieldCode);
}
