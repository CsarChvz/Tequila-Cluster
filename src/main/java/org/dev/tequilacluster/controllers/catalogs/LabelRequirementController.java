package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.LabelRequirementRequest;
import org.dev.tequilacluster.dtos.catalogs.LabelRequirementResponse;
import org.dev.tequilacluster.services.catalogs.LabelRequirementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-24/RB-305/307: label requirement catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/label-requirements")
public class LabelRequirementController {

    private final LabelRequirementService service;

    public LabelRequirementController(LabelRequirementService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabelRequirementResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public LabelRequirementResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<LabelRequirementResponse> create(@Valid @RequestBody LabelRequirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public LabelRequirementResponse update(@PathVariable UUID id, @Valid @RequestBody LabelRequirementRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
