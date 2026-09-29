package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.AuthorizedProductionAreaRequest;
import org.dev.tequilacluster.dtos.catalogs.AuthorizedProductionAreaResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.AuthorizedProductionArea;
import org.dev.tequilacluster.repositories.catalogs.AuthorizedProductionAreaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/RB-101: Administrator CRUD for authorized production areas (Denominación de Origen). */
@Service
public class AuthorizedProductionAreaService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizedProductionAreaService.class);

    private final AuthorizedProductionAreaRepository repository;

    public AuthorizedProductionAreaService(AuthorizedProductionAreaRepository repository) {
        this.repository = repository;
    }

    public List<AuthorizedProductionAreaResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public AuthorizedProductionAreaResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public AuthorizedProductionAreaResponse create(AuthorizedProductionAreaRequest request) {
        if (repository.existsByCode(request.code())) {
            log.warn("RB-101: rejected duplicate authorized production area code {}", request.code());
            throw new BusinessRuleViolationException("RB-101", "Authorized production area code already exists: " + request.code());
        }
        AuthorizedProductionArea area = new AuthorizedProductionArea();
        applyRequest(area, request);
        area.setActive(true);
        AuthorizedProductionAreaResponse response = toResponse(repository.save(area));
        log.info("Authorized production area created: {} ({})", response.code(), response.id());
        return response;
    }

    public AuthorizedProductionAreaResponse update(UUID id, AuthorizedProductionAreaRequest request) {
        AuthorizedProductionArea area = findOrThrow(id);
        applyRequest(area, request);
        AuthorizedProductionAreaResponse response = toResponse(repository.save(area));
        log.info("Authorized production area updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        AuthorizedProductionArea area = findOrThrow(id);
        area.setActive(false);
        repository.save(area);
        log.info("Authorized production area deactivated: {} ({})", area.getCode(), id);
    }

    private AuthorizedProductionArea findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("AuthorizedProductionArea", id));
    }

    private void applyRequest(AuthorizedProductionArea area, AuthorizedProductionAreaRequest request) {
        area.setCode(request.code());
        area.setName(request.name());
        area.setStateName(request.stateName());
        area.setMunicipality(request.municipality());
        area.setValidFrom(request.validFrom());
        area.setValidTo(request.validTo());
    }

    private AuthorizedProductionAreaResponse toResponse(AuthorizedProductionArea area) {
        return new AuthorizedProductionAreaResponse(
                area.getId(),
                area.getCode(),
                area.getName(),
                area.getStateName(),
                area.getMunicipality(),
                Boolean.TRUE.equals(area.getActive()),
                area.getValidFrom(),
                area.getValidTo()
        );
    }
}
