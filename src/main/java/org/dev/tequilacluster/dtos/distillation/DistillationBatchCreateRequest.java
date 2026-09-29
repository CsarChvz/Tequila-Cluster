package org.dev.tequilacluster.dtos.distillation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * FR-13/FR-14/FR-19 Request
 */
public record DistillationBatchCreateRequest(
        @NotEmpty List<HarvestLineageItem> sourceHarvestBatches,
        @NotNull LocalDate distillationDate,
        @NotNull @PositiveOrZero BigDecimal totalDistilledVolumeL,
        @NotNull @PositiveOrZero BigDecimal headsVolumeL,
        @NotNull @PositiveOrZero BigDecimal heartsVolumeL,
        @NotNull @PositiveOrZero BigDecimal tailsVolumeL,
        @NotNull @PositiveOrZero BigDecimal alcoholContentPct,
        BigDecimal cookingTemperatureC,
        BigDecimal fermentationPh,
        @NotNull @PositiveOrZero BigDecimal actualYieldL,
        boolean maturationRequired,
        LocalDate maturationStartDate,
        Integer requiredMaturationDays,
        String notes
) {
    public record HarvestLineageItem(
            @NotNull UUID harvestBatchId, 
            @NotNull @Positive BigDecimal quantityUsed, 
            @NotNull @NotBlank String unit
    ) {}
}
