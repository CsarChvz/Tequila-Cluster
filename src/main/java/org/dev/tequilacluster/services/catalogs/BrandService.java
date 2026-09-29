package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.BrandRequest;
import org.dev.tequilacluster.dtos.catalogs.BrandResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.Brand;
import org.dev.tequilacluster.repositories.catalogs.BrandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-21: Administrator CRUD for commercial brands used in bottling. */
@Service
public class BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandService.class);

    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    public List<BrandResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public BrandResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public BrandResponse create(BrandRequest request) {
        if (repository.findByName(request.name()).isPresent()) {
            log.warn("FR-21: rejected duplicate brand name {}", request.name());
            throw new BusinessRuleViolationException("FR-21", "Brand name already exists: " + request.name());
        }
        Brand brand = new Brand();
        applyRequest(brand, request);
        brand.setActive(true);
        BrandResponse response = toResponse(repository.save(brand));
        log.info("Brand created: {} ({})", response.name(), response.id());
        return response;
    }

    public BrandResponse update(UUID id, BrandRequest request) {
        Brand brand = findOrThrow(id);
        applyRequest(brand, request);
        BrandResponse response = toResponse(repository.save(brand));
        log.info("Brand updated: {} ({})", response.name(), id);
        return response;
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        Brand brand = findOrThrow(id);
        brand.setActive(false);
        repository.save(brand);
        log.info("Brand deactivated: {} ({})", brand.getName(), id);
    }

    private Brand findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Brand", id));
    }

    private void applyRequest(Brand brand, BrandRequest request) {
        brand.setName(request.name());
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                Boolean.TRUE.equals(brand.getActive())
        );
    }
}
