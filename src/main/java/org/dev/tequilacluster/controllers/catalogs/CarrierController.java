package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.CarrierRequest;
import org.dev.tequilacluster.dtos.catalogs.CarrierResponse;
import org.dev.tequilacluster.services.catalogs.CarrierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-28: carrier catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/carriers")
public class CarrierController {

    private final CarrierService service;

    public CarrierController(CarrierService service) {
        this.service = service;
    }

    @GetMapping
    public List<CarrierResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public CarrierResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<CarrierResponse> create(@Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public CarrierResponse update(@PathVariable UUID id, @Valid @RequestBody CarrierRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
