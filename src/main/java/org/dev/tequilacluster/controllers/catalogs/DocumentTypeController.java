package org.dev.tequilacluster.controllers.catalogs;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.catalogs.DocumentTypeRequest;
import org.dev.tequilacluster.dtos.catalogs.DocumentTypeResponse;
import org.dev.tequilacluster.services.catalogs.DocumentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-29: document type catalog. Read is open to any authenticated role; writes are Administrator-only. */
@RestController
@RequestMapping("/api/v1/catalogs/document-types")
public class DocumentTypeController {

    private final DocumentTypeService service;

    public DocumentTypeController(DocumentTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<DocumentTypeResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public DocumentTypeResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<DocumentTypeResponse> create(@Valid @RequestBody DocumentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public DocumentTypeResponse update(@PathVariable UUID id, @Valid @RequestBody DocumentTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
