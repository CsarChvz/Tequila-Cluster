package org.dev.tequilacluster.controllers.shared;

import org.dev.tequilacluster.dtos.alerts.ProcessAlertResponse;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * FR-44: Process alerts and resolution panel controller.
 */
@RestController
@RequestMapping("/api/v1/alerts")
public class ProcessAlertController {

    private final AlertService alertService;

    public ProcessAlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<ProcessAlertResponse>> list(
            @RequestParam(required = false, defaultValue = "OPEN") String status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID shipmentId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        List<ProcessAlertResponse> alerts = alertService.list(
                status,
                severity,
                batchId,
                shipmentId,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ProcessAlertResponse> resolve(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        ProcessAlertResponse response = alertService.resolve(
                id,
                principal != null ? principal.getUserId() : null,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }
}
