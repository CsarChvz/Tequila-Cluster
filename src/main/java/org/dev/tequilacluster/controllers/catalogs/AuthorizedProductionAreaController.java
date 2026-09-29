package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.AuthorizedProductionAreaRequest;
import org.dev.tequilacluster.dtos.catalogs.AuthorizedProductionAreaResponse;
import org.dev.tequilacluster.services.catalogs.AuthorizedProductionAreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/RB-101: authorized production area catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/authorized-production-areas")
public class AuthorizedProductionAreaController {

    private final AuthorizedProductionAreaService service;

    public AuthorizedProductionAreaController(AuthorizedProductionAreaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuthorizedProductionAreaResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AuthorizedProductionAreaResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<AuthorizedProductionAreaResponse> create(@Valid @RequestBody AuthorizedProductionAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public AuthorizedProductionAreaResponse update(@PathVariable UUID id, @Valid @RequestBody AuthorizedProductionAreaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
