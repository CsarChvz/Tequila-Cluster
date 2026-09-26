package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.TequilaCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RB-205/206/FR-20: minimum_maturation_days de la categoría condiciona el envasado. */
public interface TequilaCategoryRepository extends JpaRepository<TequilaCategory, UUID> {

    Optional<TequilaCategory> findByCode(String code);

    List<TequilaCategory> findByActiveTrue();
}
