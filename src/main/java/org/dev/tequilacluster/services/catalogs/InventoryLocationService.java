package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.InventoryLocationRequest;
import org.dev.tequilacluster.dtos.catalogs.InventoryLocationResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.InventoryLocation;
import org.dev.tequilacluster.repositories.catalogs.InventoryLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-35: Administrator CRUD for physical inventory locations. */
@Service
public class InventoryLocationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryLocationService.class);

    private final InventoryLocationRepository repository;

    public InventoryLocationService(InventoryLocationRepository repository) {
        this.repository = repository;
    }

    public List<InventoryLocationResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public InventoryLocationResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public InventoryLocationResponse create(InventoryLocationRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            log.warn("FR-35: rejected duplicate inventory location code {}", request.code());
            throw new BusinessRuleViolationException("FR-35", "Inventory location code already exists: " + request.code());
        }
        InventoryLocation location = new InventoryLocation();
        applyRequest(location, request);
        location.setActive(true);
        InventoryLocationResponse response = toResponse(repository.save(location));
        log.info("Inventory location created: {} ({})", response.code(), response.id());
        return response;
    }

    public InventoryLocationResponse update(UUID id, InventoryLocationRequest request) {
        InventoryLocation location = findOrThrow(id);
        applyRequest(location, request);
        InventoryLocationResponse response = toResponse(repository.save(location));
        log.info("Inventory location updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        InventoryLocation location = findOrThrow(id);
        location.setActive(false);
        repository.save(location);
        log.info("Inventory location deactivated: {} ({})", location.getCode(), id);
    }

    private InventoryLocation findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("InventoryLocation", id));
    }

    private void applyRequest(InventoryLocation location, InventoryLocationRequest request) {
        location.setCode(request.code());
        location.setName(request.name());
    }

    private InventoryLocationResponse toResponse(InventoryLocation location) {
        return new InventoryLocationResponse(
                location.getId(),
                location.getCode(),
                location.getName(),
                Boolean.TRUE.equals(location.getActive())
        );
    }
}
