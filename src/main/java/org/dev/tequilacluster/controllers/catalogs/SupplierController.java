package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.SupplierRequest;
import org.dev.tequilacluster.dtos.catalogs.SupplierResponse;
import org.dev.tequilacluster.services.catalogs.SupplierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * FR-04: supplier catalog CRUD. Read (list/get) is open to any authenticated role — every
 * stage's create form needs to read reference catalogs (RB-504 already requires login via
 * SecurityConfig). Only create/update/deactivate are Administrator-only.
 */
@RestController
@RequestMapping("/api/v1/catalogs/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> list() {
        return supplierService.list();
    }

    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable UUID id) {
        return supplierService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        supplierService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
