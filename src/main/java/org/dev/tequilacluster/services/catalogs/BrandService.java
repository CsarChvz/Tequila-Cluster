package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.BrandRequest;
import org.dev.tequilacluster.dtos.catalogs.BrandResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.Brand;
import org.dev.tequilacluster.repositories.catalogs.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-21: Administrator CRUD for commercial brands used in bottling. */
@Service
public class BrandService {

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
            throw new BusinessRuleViolationException("FR-21", "Brand name already exists: " + request.name());
        }
        Brand brand = new Brand();
        applyRequest(brand, request);
        brand.setActive(true);
        return toResponse(repository.save(brand));
    }

    public BrandResponse update(UUID id, BrandRequest request) {
        Brand brand = findOrThrow(id);
        applyRequest(brand, request);
        return toResponse(repository.save(brand));
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        Brand brand = findOrThrow(id);
        brand.setActive(false);
        repository.save(brand);
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
