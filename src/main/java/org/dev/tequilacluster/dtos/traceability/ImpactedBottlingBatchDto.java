package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Summary of bottling batch impacted by forward traceability.
 */
public record ImpactedBottlingBatchDto(
        UUID bottlingBatchId,
        String traceabilityCode,
        String productionLotNumber,
        String brandName,
        String categoryName,
        LocalDate bottlingDate,
        Integer bottleCapacityMl,
        BigDecimal totalVolumeL,
        Integer unitsBottled,
        Map<String, Long> unitCountsByStatus
) {}
