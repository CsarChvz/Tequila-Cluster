package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.DocumentTypeRequest;
import org.dev.tequilacluster.dtos.catalogs.DocumentTypeResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.DocumentType;
import org.dev.tequilacluster.repositories.catalogs.DocumentTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-29: Administrator CRUD for shipment document types. */
@Service
public class DocumentTypeService {

    private static final Logger log = LoggerFactory.getLogger(DocumentTypeService.class);

    private final DocumentTypeRepository repository;

    public DocumentTypeService(DocumentTypeRepository repository) {
        this.repository = repository;
    }

    public List<DocumentTypeResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public DocumentTypeResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public DocumentTypeResponse create(DocumentTypeRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            log.warn("FR-29: rejected duplicate document type code {}", request.code());
            throw new BusinessRuleViolationException("FR-29", "Document type code already exists: " + request.code());
        }
        DocumentType type = new DocumentType();
        applyRequest(type, request);
        type.setActive(true);
        DocumentTypeResponse response = toResponse(repository.save(type));
        log.info("Document type created: {} ({})", response.code(), response.id());
        return response;
    }

    public DocumentTypeResponse update(UUID id, DocumentTypeRequest request) {
        DocumentType type = findOrThrow(id);
        applyRequest(type, request);
        DocumentTypeResponse response = toResponse(repository.save(type));
        log.info("Document type updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        DocumentType type = findOrThrow(id);
        type.setActive(false);
        repository.save(type);
        log.info("Document type deactivated: {} ({})", type.getCode(), id);
    }

    private DocumentType findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("DocumentType", id));
    }

    private void applyRequest(DocumentType type, DocumentTypeRequest request) {
        type.setCode(request.code());
        type.setName(request.name());
    }

    private DocumentTypeResponse toResponse(DocumentType type) {
        return new DocumentTypeResponse(
                type.getId(),
                type.getCode(),
                type.getName(),
                Boolean.TRUE.equals(type.getActive())
        );
    }
}
