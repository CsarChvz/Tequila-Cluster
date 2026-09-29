package org.dev.tequilacluster.controllers.inventory;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.inventory.InventoryMovementCreateRequest;
import org.dev.tequilacluster.dtos.inventory.InventoryMovementResponse;
import org.dev.tequilacluster.dtos.inventory.InventoryReconciliationResponse;
import org.dev.tequilacluster.dtos.inventory.InventoryStockResponse;
import org.dev.tequilacluster.services.inventory.InventoryMovementService;
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
 * Controlador de inventario (Kárdex y Reconciliación): FR-35, FR-36, RB-506.
 * Asignado a la etapa LOGISTICS conforme al catálogo de process_stage y permisos RBAC.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryMovementService inventoryMovementService;
    private final StagePermissionService stagePermissionService;

    public InventoryController(
            InventoryMovementService inventoryMovementService,
            StagePermissionService stagePermissionService
    ) {
        this.inventoryMovementService = inventoryMovementService;
        this.stagePermissionService = stagePermissionService;
    }

    /**
     * FR-35: Registra un movimiento de inventario en el Kárdex.
     */
    @PostMapping("/movements")
    public ResponseEntity<InventoryMovementResponse> create(
            @Valid @RequestBody InventoryMovementCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.CREATE);
        InventoryMovementResponse response = inventoryMovementService.create(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * FR-35: Consulta el historial de movimientos de inventario con filtros opcionales.
     */
    @GetMapping("/movements")
    public ResponseEntity<List<InventoryMovementResponse>> list(
            @RequestParam(required = false) UUID bottlingBatchId,
            @RequestParam(required = false) UUID locationId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.VIEW);
        List<InventoryMovementResponse> response = inventoryMovementService.listMovements(bottlingBatchId, locationId);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-36 / RB-506: Consulta de existencias actuales calculadas a partir del Kárdex por lote y ubicación.
     */
    @GetMapping("/stock")
    public ResponseEntity<List<InventoryStockResponse>> getStock(
            @RequestParam(required = false) UUID bottlingBatchId,
            @RequestParam(required = false) UUID locationId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.VIEW);
        List<InventoryStockResponse> response = inventoryMovementService.getStock(bottlingBatchId, locationId);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-36 / RB-506: Reconciliación global de inventario y detección de discrepancias para un lote de envasado.
     */
    @GetMapping("/reconcile/{bottlingBatchId}")
    public ResponseEntity<InventoryReconciliationResponse> reconcile(
            @PathVariable UUID bottlingBatchId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        stagePermissionService.assertAllowed(principal.getRoleCodes(), ProcessStageCodes.LOGISTICS, StageAction.VIEW);
        InventoryReconciliationResponse response = inventoryMovementService.reconcile(bottlingBatchId);
        return ResponseEntity.ok(response);
    }
}
