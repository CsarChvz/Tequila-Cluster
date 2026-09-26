package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.LabelRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-24/RB-305,307: requisitos obligatorios de etiqueta a validar antes de completar el envasado. */
public interface LabelRequirementRepository extends JpaRepository<LabelRequirement, UUID> {

    Optional<LabelRequirement> findByCode(String code);

    List<LabelRequirement> findByActiveTrue();

    List<LabelRequirement> findByRequiredTrueAndActiveTrue();
}
