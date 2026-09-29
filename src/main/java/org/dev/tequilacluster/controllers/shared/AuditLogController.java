package org.dev.tequilacluster.controllers.shared;

import org.dev.tequilacluster.dtos.shared.AuditLogResponse;
import org.dev.tequilacluster.services.shared.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** FR-42/FR-43: audit log activity feed. Administrator and Auditor only (RB-504: Auditor = view-only, every stage). */
@RestController
@RequestMapping("/api/v1/audit-log")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'AUDITOR')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> list(@RequestParam(required = false, defaultValue = "200") int limit) {
        return ResponseEntity.ok(auditLogService.listRecent(limit));
    }
}
