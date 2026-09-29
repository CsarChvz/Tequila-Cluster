package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.LabelRequirementRequest;
import org.dev.tequilacluster.dtos.catalogs.LabelRequirementResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.LabelRequirement;
import org.dev.tequilacluster.repositories.catalogs.LabelRequirementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-24/RB-305/307: Administrator CRUD for mandatory/optional bottling label fields. */
@Service
public class LabelRequirementService {

    private static final Logger log = LoggerFactory.getLogger(LabelRequirementService.class);

    private final LabelRequirementRepository repository;

    public LabelRequirementService(LabelRequirementRepository repository) {
        this.repository = repository;
    }

    public List<LabelRequirementResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public LabelRequirementResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public LabelRequirementResponse create(LabelRequirementRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            log.warn("FR-24: rejected duplicate label requirement code {}", request.code());
            throw new BusinessRuleViolationException("FR-24", "Label requirement code already exists: " + request.code());
        }
        LabelRequirement requirement = new LabelRequirement();
        applyRequest(requirement, request);
        requirement.setActive(true);
        LabelRequirementResponse response = toResponse(repository.save(requirement));
        log.info("Label requirement created: {} ({})", response.code(), response.id());
        return response;
    }

    public LabelRequirementResponse update(UUID id, LabelRequirementRequest request) {
        LabelRequirement requirement = findOrThrow(id);
        applyRequest(requirement, request);
        LabelRequirementResponse response = toResponse(repository.save(requirement));
        log.info("Label requirement updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        LabelRequirement requirement = findOrThrow(id);
        requirement.setActive(false);
        repository.save(requirement);
        log.info("Label requirement deactivated: {} ({})", requirement.getCode(), id);
    }

    private LabelRequirement findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("LabelRequirement", id));
    }

    private void applyRequest(LabelRequirement requirement, LabelRequirementRequest request) {
        requirement.setCode(request.code());
        requirement.setDisplayName(request.displayName());
        requirement.setRequired(request.required());
    }

    private LabelRequirementResponse toResponse(LabelRequirement requirement) {
        return new LabelRequirementResponse(
                requirement.getId(),
                requirement.getCode(),
                requirement.getDisplayName(),
                Boolean.TRUE.equals(requirement.getRequired()),
                Boolean.TRUE.equals(requirement.getActive())
        );
    }
}
