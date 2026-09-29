package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.AgaveFieldRequest;
import org.dev.tequilacluster.dtos.catalogs.AgaveFieldResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.AgaveField;
import org.dev.tequilacluster.models.catalogs.AuthorizedProductionArea;
import org.dev.tequilacluster.repositories.catalogs.AgaveFieldRepository;
import org.dev.tequilacluster.repositories.catalogs.AuthorizedProductionAreaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-06: Administrator CRUD for agave fields, linked to an authorized production area. */
@Service
public class AgaveFieldService {

    private static final Logger log = LoggerFactory.getLogger(AgaveFieldService.class);

    private final AgaveFieldRepository repository;
    private final AuthorizedProductionAreaRepository authorizedProductionAreaRepository;

    public AgaveFieldService(AgaveFieldRepository repository,
                              AuthorizedProductionAreaRepository authorizedProductionAreaRepository) {
        this.repository = repository;
        this.authorizedProductionAreaRepository = authorizedProductionAreaRepository;
    }

    /**
     * @Transactional is required here (and on {@link #get}) even though this only reads data:
     * {@code AgaveField.authorizedArea} is a lazy association, and {@link #toResponse} reads
     * its code outside the repository call — without an open session the proxy throws
     * LazyInitializationException as soon as any non-Administrator role (which now has read
     * access to this catalog) calls this endpoint.
     */
    @Transactional(readOnly = true)
    public List<AgaveFieldResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgaveFieldResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public AgaveFieldResponse create(AgaveFieldRequest request) {
        if (repository.existsByFieldCode(request.fieldCode())) {
            log.warn("FR-06: rejected duplicate agave field code {}", request.fieldCode());
            throw new BusinessRuleViolationException("FR-06", "Agave field code already exists: " + request.fieldCode());
        }
        AgaveField field = new AgaveField();
        applyRequest(field, request);
        field.setActive(true);
        AgaveFieldResponse response = toResponse(repository.save(field));
        log.info("Agave field created: {} ({})", response.fieldCode(), response.id());
        return response;
    }

    public AgaveFieldResponse update(UUID id, AgaveFieldRequest request) {
        AgaveField field = findOrThrow(id);
        applyRequest(field, request);
        AgaveFieldResponse response = toResponse(repository.save(field));
        log.info("Agave field updated: {} ({})", response.fieldCode(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        AgaveField field = findOrThrow(id);
        field.setActive(false);
        repository.save(field);
        log.info("Agave field deactivated: {} ({})", field.getFieldCode(), id);
    }

    private AgaveField findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("AgaveField", id));
    }

    private void applyRequest(AgaveField field, AgaveFieldRequest request) {
        AuthorizedProductionArea area = authorizedProductionAreaRepository.findById(request.authorizedAreaId())
                .orElseThrow(() -> NotFoundException.of("AuthorizedProductionArea", request.authorizedAreaId()));
        field.setFieldCode(request.fieldCode());
        field.setName(request.name());
        field.setAuthorizedArea(area);
        field.setAddressText(request.addressText());
        field.setLatitude(request.latitude());
        field.setLongitude(request.longitude());
    }

    private AgaveFieldResponse toResponse(AgaveField field) {
        return new AgaveFieldResponse(
                field.getId(),
                field.getFieldCode(),
                field.getName(),
                field.getAuthorizedArea() != null ? field.getAuthorizedArea().getId() : null,
                field.getAuthorizedArea() != null ? field.getAuthorizedArea().getCode() : null,
                field.getAddressText(),
                field.getLatitude(),
                field.getLongitude(),
                Boolean.TRUE.equals(field.getActive())
        );
    }
}
