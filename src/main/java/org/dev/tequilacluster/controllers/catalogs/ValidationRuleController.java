package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.ValidationRuleRequest;
import org.dev.tequilacluster.dtos.catalogs.ValidationRuleResponse;
import org.dev.tequilacluster.services.catalogs.ValidationRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/RB-103/203/204/207: validation rule catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/validation-rules")
public class ValidationRuleController {

    private final ValidationRuleService service;

    public ValidationRuleController(ValidationRuleService service) {
        this.service = service;
    }

    @GetMapping
    public List<ValidationRuleResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ValidationRuleResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ValidationRuleResponse> create(@Valid @RequestBody ValidationRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ValidationRuleResponse update(@PathVariable UUID id, @Valid @RequestBody ValidationRuleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
