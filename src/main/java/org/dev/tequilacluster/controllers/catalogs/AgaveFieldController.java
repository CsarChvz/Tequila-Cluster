package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.AgaveFieldRequest;
import org.dev.tequilacluster.dtos.catalogs.AgaveFieldResponse;
import org.dev.tequilacluster.services.catalogs.AgaveFieldService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-06: agave field catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/agave-fields")
public class AgaveFieldController {

    private final AgaveFieldService service;

    public AgaveFieldController(AgaveFieldService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgaveFieldResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AgaveFieldResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<AgaveFieldResponse> create(@Valid @RequestBody AgaveFieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public AgaveFieldResponse update(@PathVariable UUID id, @Valid @RequestBody AgaveFieldRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
