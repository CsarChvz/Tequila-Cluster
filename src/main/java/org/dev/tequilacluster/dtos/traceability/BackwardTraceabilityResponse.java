package org.dev.tequilacluster.dtos.traceability;

import java.util.List;

/**
 * FR-37: Backward traceability response.
 */
public record BackwardTraceabilityResponse(
        String queryCode,
        String searchType,
        TraceabilityNodeDto targetBatch,
        BottledUnitDetailDto bottledUnit,
        List<TraceabilityNodeDto> nodes,
        List<BatchLineageLinkDto> links,
        List<HarvestOriginDto> harvestOrigins
) {}
