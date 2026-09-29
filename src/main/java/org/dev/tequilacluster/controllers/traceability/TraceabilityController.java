package org.dev.tequilacluster.controllers.traceability;

import org.dev.tequilacluster.dtos.traceability.BackwardTraceabilityResponse;
import org.dev.tequilacluster.dtos.traceability.BatchHistoryResponse;
import org.dev.tequilacluster.dtos.traceability.ForwardTraceabilityResponse;
import org.dev.tequilacluster.services.traceability.TraceabilityQueryService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Traceability (FR-37, FR-38, FR-43).
 */
@RestController
@RequestMapping("/api/v1/traceability")
public class TraceabilityController {

    private final TraceabilityQueryService traceabilityQueryService;

    public TraceabilityController(TraceabilityQueryService traceabilityQueryService) {
        this.traceabilityQueryService = traceabilityQueryService;
    }

    /**
     * FR-37: Backward traceability via query parameter.
     */
    @GetMapping("/backward")
    public ResponseEntity<BackwardTraceabilityResponse> backward(
            @RequestParam("code") String code,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BackwardTraceabilityResponse response = traceabilityQueryService.backward(
                code,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * FR-37: Backward traceability via path variable.
     */
    @GetMapping("/backward/{code}")
    public ResponseEntity<BackwardTraceabilityResponse> backwardByPath(
            @PathVariable("code") String code,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BackwardTraceabilityResponse response = traceabilityQueryService.backward(
                code,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * FR-38: Forward traceability from Batch, Supplier, or AgaveField.
     */
    @GetMapping("/forward")
    public ResponseEntity<ForwardTraceabilityResponse> forward(
            @RequestParam(required = false) UUID batchId,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID fieldId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        ForwardTraceabilityResponse response = traceabilityQueryService.forward(
                batchId,
                supplierId,
                fieldId,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * FR-43: Full lifecycle history of a batch by traceabilityCode.
     */
    @GetMapping("/history/{traceabilityCode}")
    public ResponseEntity<BatchHistoryResponse> getHistory(
            @PathVariable("traceabilityCode") String traceabilityCode,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BatchHistoryResponse response = traceabilityQueryService.getHistory(
                traceabilityCode,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Alias for batch history by traceabilityCode.
     */
    @GetMapping("/{traceabilityCode}")
    public ResponseEntity<BatchHistoryResponse> getByCode(
            @PathVariable("traceabilityCode") String traceabilityCode,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        BatchHistoryResponse response = traceabilityQueryService.getHistory(
                traceabilityCode,
                principal != null ? principal.getRoleCodes() : List.of()
        );
        return ResponseEntity.ok(response);
    }
}
