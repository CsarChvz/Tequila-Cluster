package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.TequilaCategoryRequest;
import org.dev.tequilacluster.dtos.catalogs.TequilaCategoryResponse;
import org.dev.tequilacluster.services.catalogs.TequilaCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/RB-205/RB-206: tequila category catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/tequila-categories")
public class TequilaCategoryController {

    private final TequilaCategoryService service;

    public TequilaCategoryController(TequilaCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<TequilaCategoryResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public TequilaCategoryResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<TequilaCategoryResponse> create(@Valid @RequestBody TequilaCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public TequilaCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody TequilaCategoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
