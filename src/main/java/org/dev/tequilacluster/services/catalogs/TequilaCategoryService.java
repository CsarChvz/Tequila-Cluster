package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.TequilaCategoryRequest;
import org.dev.tequilacluster.dtos.catalogs.TequilaCategoryResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.TequilaCategory;
import org.dev.tequilacluster.repositories.catalogs.TequilaCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/RB-205/RB-206: Administrator CRUD for tequila categories and minimum maturation days. */
@Service
public class TequilaCategoryService {

    private static final Logger log = LoggerFactory.getLogger(TequilaCategoryService.class);

    private final TequilaCategoryRepository repository;

    public TequilaCategoryService(TequilaCategoryRepository repository) {
        this.repository = repository;
    }

    public List<TequilaCategoryResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public TequilaCategoryResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public TequilaCategoryResponse create(TequilaCategoryRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            log.warn("RB-206: rejected duplicate tequila category code {}", request.code());
            throw new BusinessRuleViolationException("RB-206", "Tequila category code already exists: " + request.code());
        }
        TequilaCategory category = new TequilaCategory();
        applyRequest(category, request);
        category.setActive(true);
        TequilaCategoryResponse response = toResponse(repository.save(category));
        log.info("Tequila category created: {} ({})", response.code(), response.id());
        return response;
    }

    public TequilaCategoryResponse update(UUID id, TequilaCategoryRequest request) {
        TequilaCategory category = findOrThrow(id);
        applyRequest(category, request);
        TequilaCategoryResponse response = toResponse(repository.save(category));
        log.info("Tequila category updated: {} ({})", response.code(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        TequilaCategory category = findOrThrow(id);
        category.setActive(false);
        repository.save(category);
        log.info("Tequila category deactivated: {} ({})", category.getCode(), id);
    }

    private TequilaCategory findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("TequilaCategory", id));
    }

    private void applyRequest(TequilaCategory category, TequilaCategoryRequest request) {
        category.setCode(request.code());
        category.setName(request.name());
        category.setMinimumMaturationDays(request.minimumMaturationDays());
    }

    private TequilaCategoryResponse toResponse(TequilaCategory category) {
        return new TequilaCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getMinimumMaturationDays(),
                Boolean.TRUE.equals(category.getActive())
        );
    }
}
