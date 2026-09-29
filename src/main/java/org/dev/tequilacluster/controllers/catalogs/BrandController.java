package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.BrandRequest;
import org.dev.tequilacluster.dtos.catalogs.BrandResponse;
import org.dev.tequilacluster.services.catalogs.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-21: brand catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/brands")
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @GetMapping
    public List<BrandResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public BrandResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public BrandResponse update(@PathVariable UUID id, @Valid @RequestBody BrandRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
