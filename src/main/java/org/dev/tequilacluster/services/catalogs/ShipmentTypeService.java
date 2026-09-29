package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.ShipmentTypeRequest;
import org.dev.tequilacluster.dtos.catalogs.ShipmentTypeResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.ShipmentType;
import org.dev.tequilacluster.repositories.catalogs.ShipmentTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-28/FR-29: Administrator CRUD for shipment types. */
@Service
public class ShipmentTypeService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentTypeService.class);

    private final ShipmentTypeRepository repository;

    public ShipmentTypeService(ShipmentTypeRepository repository) {
        this.repository = repository;
    }

    public List<ShipmentTypeResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ShipmentTypeResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public ShipmentTypeResponse create(ShipmentTypeRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            log.warn("FR-28: rejected duplicate shipment type code {}", request.code());
            throw new BusinessRuleViolationException("FR-28", "Shipment type code already exists: " + request.code());
        }
        ShipmentType type = new ShipmentType();
        applyRequest(type, request);
        type.setActive(true);
        ShipmentTypeResponse response = toResponse(repository.save(type));
        log.info("Shipment type created: {} ({})", response.code(), response.id());
        return response;
    }

    public ShipmentTypeResponse update(UUID id, ShipmentTypeRequest request) {
        ShipmentType type = findOrThrow(id);
        applyRequest(type, request);
        ShipmentTypeResponse response = toResponse(repository.save(type));
        log.info("Shipment type updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        ShipmentType type = findOrThrow(id);
        type.setActive(false);
        repository.save(type);
        log.info("Shipment type deactivated: {} ({})", type.getCode(), id);
    }

    private ShipmentType findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("ShipmentType", id));
    }

    private void applyRequest(ShipmentType type, ShipmentTypeRequest request) {
        type.setCode(request.code());
        type.setName(request.name());
    }

    private ShipmentTypeResponse toResponse(ShipmentType type) {
        return new ShipmentTypeResponse(
                type.getId(),
                type.getCode(),
                type.getName(),
                Boolean.TRUE.equals(type.getActive())
        );
    }
}
