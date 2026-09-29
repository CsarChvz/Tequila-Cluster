package org.dev.tequilacluster.repositories.catalogs;

import org.dev.tequilacluster.models.catalogs.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

    Optional<DocumentType> findByCode(String code);

    List<DocumentType> findByActiveTrue();
}
