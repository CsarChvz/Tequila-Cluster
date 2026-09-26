package org.dev.tequilacluster.controllers.distillation;

import org.dev.tequilacluster.dtos.distillation.DistillationBatchCreateRequest;
import org.dev.tequilacluster.services.distillation.DistillationBatchService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * TODO(dev-a): FR-12 to FR-19. Add GET/{id}, /complete and /cancel endpoints mirroring
 * {@code JimaBatchController} once the service is implemented, guarded by
 * {@code StagePermissionService.assertAllowed(..., ProcessStageCodes.DISTILLATION, ...)}.
 */
@RestController
@RequestMapping("/api/v1/distillation/batches")
public class DistillationBatchController {

    private final DistillationBatchService distillationBatchService;

    public DistillationBatchController(DistillationBatchService distillationBatchService) {
        this.distillationBatchService = distillationBatchService;
    }

    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody DistillationBatchCreateRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(distillationBatchService.create(request, principal.getUserId()));
    }
}
