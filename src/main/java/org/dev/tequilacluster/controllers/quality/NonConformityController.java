package org.dev.tequilacluster.controllers.quality;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.quality.NonConformityCreateRequest;
import org.dev.tequilacluster.dtos.quality.NonConformityResponse;
import org.dev.tequilacluster.dtos.quality.NonConformityStatusUpdateRequest;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;
import org.dev.tequilacluster.services.quality.NonConformityService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Quality stage controller: FR-33, RB-407 (NonConformity management).
 */
@RestController
@RequestMapping("/api/v1/quality/non-conformities")
public class NonConformityController {

    private final NonConformityService nonConformityService;

    public NonConformityController(NonConformityService nonConformityService) {
        this.nonConformityService = nonConformityService;
    }

    @PostMapping
    public ResponseEntity<NonConformityResponse> create(
            @Valid @RequestBody NonConformityCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        NonConformityResponse response = nonConformityService.create(
                request,
                principal != null ? principal.getUserId() : null,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NonConformityResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        NonConformityResponse response = nonConformityService.getById(
                id,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NonConformityResponse>> list(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) NonConformityStatus status,
            @RequestParam(required = false) NonConformitySeverity severity,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        List<NonConformityResponse> response = nonConformityService.list(
                batchId,
                status,
                severity,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<NonConformityResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody NonConformityStatusUpdateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        NonConformityResponse response = nonConformityService.updateStatus(
                id,
                request,
                principal != null ? principal.getUserId() : null,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }
}
