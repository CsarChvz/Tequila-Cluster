package org.dev.tequilacluster.dtos.traceability;

import java.util.List;
import java.util.UUID;

/**
 * FR-38: Forward traceability response.
 */
public record ForwardTraceabilityResponse(
        String originType,
        UUID originId,
        List<TraceabilityNodeDto> nodes,
        List<BatchLineageLinkDto> links,
        List<ImpactedBottlingBatchDto> impactedBottlingBatches,
        List<ImpactedShipmentDto> impactedShipments
) {}
