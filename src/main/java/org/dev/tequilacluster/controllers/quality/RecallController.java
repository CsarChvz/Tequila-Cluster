package org.dev.tequilacluster.controllers.quality;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.quality.RecallCreateRequest;
import org.dev.tequilacluster.dtos.quality.RecallResponse;
import org.dev.tequilacluster.dtos.quality.RecallStatusUpdateRequest;
import org.dev.tequilacluster.dtos.quality.RecallUnitResponse;
import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import org.dev.tequilacluster.models.quality.enums.RecallType;
import org.dev.tequilacluster.services.quality.RecallService;
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
 * Quality stage controller: FR-34, RB-407 (Recall of products).
 */
@RestController
@RequestMapping("/api/v1/quality/recalls")
public class RecallController {

    private final RecallService recallService;

    public RecallController(RecallService recallService) {
        this.recallService = recallService;
    }

    @PostMapping
    public ResponseEntity<RecallResponse> create(
            @Valid @RequestBody RecallCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        RecallResponse response = recallService.create(
                request,
                principal != null ? principal.getUserId() : null,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecallResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        RecallResponse response = recallService.getById(
                id,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/units")
    public ResponseEntity<List<RecallUnitResponse>> getUnits(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        List<RecallUnitResponse> response = recallService.getUnits(
                id,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RecallResponse>> list(
            @RequestParam(required = false) UUID sourceBatchId,
            @RequestParam(required = false) RecallStatus status,
            @RequestParam(required = false) RecallType recallType,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        List<RecallResponse> response = recallService.list(
                sourceBatchId,
                status,
                recallType,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RecallResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody RecallStatusUpdateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        RecallResponse response = recallService.updateStatus(
                id,
                request,
                principal != null ? principal.getUserId() : null,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }
}
