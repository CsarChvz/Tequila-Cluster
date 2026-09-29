package org.dev.tequilacluster.controllers.bottling;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.bottling.BottlingBatchCreateRequest;
import org.dev.tequilacluster.dtos.bottling.BottlingBatchResponse;
import org.dev.tequilacluster.dtos.bottling.TaxLabelBulkCreateRequest;
import org.dev.tequilacluster.dtos.bottling.TaxLabelResponse;
import org.dev.tequilacluster.dtos.shared.CancelRequest;
import org.dev.tequilacluster.services.bottling.BottlingBatchService;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.BatchLifecycleService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bottling/batches")
public class BottlingBatchController {

    private final BottlingBatchService bottlingBatchService;
    private final BatchLifecycleService batchLifecycleService;
    private final StagePermissionService stagePermissionService;

    public BottlingBatchController(BottlingBatchService bottlingBatchService,
                                   BatchLifecycleService batchLifecycleService,
                                   StagePermissionService stagePermissionService) {
        this.bottlingBatchService = bottlingBatchService;
        this.batchLifecycleService = batchLifecycleService;
        this.stagePermissionService = stagePermissionService;
    }

    @PostMapping
    public ResponseEntity<BottlingBatchResponse> create(@Valid @RequestBody BottlingBatchCreateRequest request,
                                                         @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.CREATE);
        BottlingBatchResponse response = bottlingBatchService.create(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<BottlingBatchResponse> get(@PathVariable UUID batchId,
                                                      @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.VIEW);
        return ResponseEntity.ok(bottlingBatchService.get(batchId));
    }

    @GetMapping
    public ResponseEntity<List<BottlingBatchResponse>> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.VIEW);
        return ResponseEntity.ok(bottlingBatchService.list());
    }

    @GetMapping("/tax-labels")
    public ResponseEntity<List<TaxLabelResponse>> listTaxLabels(
            @RequestParam(required = false, defaultValue = "AVAILABLE") String status,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.VIEW);
        return ResponseEntity.ok(bottlingBatchService.listTaxLabels(status));
    }

    /** Administrator-only: bulk-load SAT tax label folios (fiscal marbetes supplied externally). */
    @PostMapping("/tax-labels/bulk")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<TaxLabelResponse>> bulkCreateTaxLabels(@Valid @RequestBody TaxLabelBulkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bottlingBatchService.bulkCreateTaxLabels(request));
    }

    @PostMapping("/{batchId}/complete")
    public ResponseEntity<Void> complete(@PathVariable UUID batchId,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.COMPLETE);
        bottlingBatchService.complete(batchId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{batchId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID batchId,
                                        @Valid @RequestBody CancelRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.BOTTLING, StageAction.UPDATE);
        batchLifecycleService.cancel(batchId, principal.getUserId(), request.reason());
        return ResponseEntity.noContent().build();
    }
}
