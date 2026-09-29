package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.ShipmentTypeRequest;
import org.dev.tequilacluster.dtos.catalogs.ShipmentTypeResponse;
import org.dev.tequilacluster.services.catalogs.ShipmentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-28/FR-29: shipment type catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/shipment-types")
public class ShipmentTypeController {

    private final ShipmentTypeService service;

    public ShipmentTypeController(ShipmentTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<ShipmentTypeResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ShipmentTypeResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ShipmentTypeResponse> create(@Valid @RequestBody ShipmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ShipmentTypeResponse update(@PathVariable UUID id, @Valid @RequestBody ShipmentTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
