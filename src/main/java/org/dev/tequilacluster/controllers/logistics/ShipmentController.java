package org.dev.tequilacluster.controllers.logistics;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.logistics.ShipmentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentCreateRequest;
import org.dev.tequilacluster.dtos.logistics.ShipmentDocumentResponse;
import org.dev.tequilacluster.dtos.logistics.ShipmentResponse;
import org.dev.tequilacluster.services.logistics.ShipmentService;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Logistics stage controller: FR-26 a FR-32 (Shipments).
 */
@RestController
@RequestMapping("/api/v1/logistics/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final StagePermissionService stagePermissionService;

    public ShipmentController(
            ShipmentService shipmentService,
            StagePermissionService stagePermissionService
    ) {
        this.shipmentService = shipmentService;
        this.stagePermissionService = stagePermissionService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody ShipmentCreateRequest request,
                                                    @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.CREATE);
        ShipmentResponse response = shipmentService.create(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> get(@PathVariable UUID id,
                                                 @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.VIEW);
        return ResponseEntity.ok(shipmentService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.VIEW);
        return ResponseEntity.ok(shipmentService.list());
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ShipmentDocumentResponse> addDocument(@PathVariable UUID id,
                                                                @Valid @RequestBody ShipmentDocumentCreateRequest request,
                                                                @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.UPDATE);
        ShipmentDocumentResponse response = shipmentService.addDocument(id, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/start-transit")
    public ResponseEntity<ShipmentResponse> startTransit(@PathVariable UUID id,
                                                         @AuthenticationPrincipal AppUserPrincipal principal) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.UPDATE);
        ShipmentResponse response = shipmentService.startTransit(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }
}
