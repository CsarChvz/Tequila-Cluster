package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * FR-43: Detailed distillation stage view for batch history.
 */
public record DistillationBatchDetailDto(
        LocalDate distillationDate,
        BigDecimal totalDistilledVolumeL,
        BigDecimal headsVolumeL,
        BigDecimal heartsVolumeL,
        BigDecimal tailsVolumeL,
        BigDecimal alcoholContentPct,
        BigDecimal cookingTemperatureC,
        BigDecimal fermentationPh,
        BigDecimal actualYieldL,
        BigDecimal estimatedYieldLSnapshot,
        Boolean maturationRequired,
        LocalDate maturationStartDate,
        Integer requiredMaturationDays,
        Instant readyForBottlingAt,
        String notes
) {}
