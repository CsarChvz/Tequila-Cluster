package org.dev.tequilacluster.controllers.harvest;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.harvest.JimaBatchCreateRequest;
import org.dev.tequilacluster.dtos.harvest.JimaBatchResponse;
import org.dev.tequilacluster.dtos.shared.CancelRequest;
import org.dev.tequilacluster.services.harvest.JimaBatchService;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** FR-05 to FR-11: harvest (Jima) batches. Reference controller — replicate this shape for the other stages. */
@RestController
@RequestMapping("/api/v1/harvest/batches")
public class JimaBatchController {

    private final JimaBatchService jimaBatchService;
    private final BatchLifecycleService batchLifecycleService;
    private final StagePermissionService stagePermissionService;

    public JimaBatchController(
            JimaBatchService jimaBatchService,
            BatchLifecycleService batchLifecycleService,
            StagePermissionService stagePermissionService
    ) {
        this.jimaBatchService = jimaBatchService;
        this.batchLifecycleService = batchLifecycleService;
        this.stagePermissionService = stagePermissionService;
    }

    @PostMapping
    public ResponseEntity<JimaBatchResponse> create(@Valid @RequestBody JimaBatchCreateRequest request,
                                                     @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.HARVEST, StageAction.CREATE);
        JimaBatchResponse response = jimaBatchService.create(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<JimaBatchResponse> get(@PathVariable UUID batchId,
                                                  @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.HARVEST, StageAction.VIEW);
        return ResponseEntity.ok(jimaBatchService.get(batchId));
    }

    @PostMapping("/{batchId}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID batchId,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.HARVEST, StageAction.COMPLETE);
        batchLifecycleService.complete(batchId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{batchId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID batchId,
                                        @Valid @RequestBody CancelRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.HARVEST, StageAction.UPDATE);
        batchLifecycleService.cancel(batchId, principal.getUserId(), request.reason());
        return ResponseEntity.noContent().build();
    }
}
