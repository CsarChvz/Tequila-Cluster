package org.dev.tequilacluster.controllers.distillation;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.distillation.DistillationBatchCreateRequest;
import org.dev.tequilacluster.dtos.distillation.DistillationBatchResponse;
import org.dev.tequilacluster.dtos.shared.CancelRequest;
import org.dev.tequilacluster.services.distillation.DistillationBatchService;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/distillation/batches")
public class DistillationBatchController {

    private final DistillationBatchService distillationBatchService;
    private final BatchLifecycleService batchLifecycleService;
    private final StagePermissionService stagePermissionService;

    public DistillationBatchController(
            DistillationBatchService distillationBatchService,
            BatchLifecycleService batchLifecycleService,
            StagePermissionService stagePermissionService
    ) {
        this.distillationBatchService = distillationBatchService;
        this.batchLifecycleService = batchLifecycleService;
        this.stagePermissionService = stagePermissionService;
    }

    @PostMapping
    public ResponseEntity<DistillationBatchResponse> create(@Valid @RequestBody DistillationBatchCreateRequest request,
                                                             @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.DISTILLATION, StageAction.CREATE);
        DistillationBatchResponse response = distillationBatchService.create(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<DistillationBatchResponse> get(@PathVariable UUID batchId,
                                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.DISTILLATION, StageAction.VIEW);
        return ResponseEntity.ok(distillationBatchService.get(batchId));
    }

    @GetMapping
    public ResponseEntity<List<DistillationBatchResponse>> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.DISTILLATION, StageAction.VIEW);
        return ResponseEntity.ok(distillationBatchService.list());
    }

    @PostMapping("/{batchId}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID batchId,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.DISTILLATION, StageAction.COMPLETE);
        distillationBatchService.complete(batchId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{batchId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID batchId,
                                        @Valid @RequestBody CancelRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.DISTILLATION, StageAction.UPDATE);
        batchLifecycleService.cancel(batchId, principal.getUserId(), request.reason());
        return ResponseEntity.noContent().build();
    }
}
