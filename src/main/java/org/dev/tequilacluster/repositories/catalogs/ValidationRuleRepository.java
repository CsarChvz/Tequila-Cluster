package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RB-103/203/204/207: reglas configurables de rango/tolerancia por etapa+parámetro. */
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, UUID> {

    List<ValidationRule> findByStageCodeAndActiveTrue(String stageCode);

    /** Regla vigente de un parámetro para una etapa (valid_to null o futuro). */
    Optional<ValidationRule> findByStageCodeAndParameterCodeAndActiveTrue(String stageCode, String parameterCode);

    List<ValidationRule> findByStageCode(String stageCode);
}
