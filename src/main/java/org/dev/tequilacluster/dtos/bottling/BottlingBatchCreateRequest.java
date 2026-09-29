package org.dev.tequilacluster.dtos.bottling;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** FR-20 to FR-25: request to create a bottling batch. */
public record BottlingBatchCreateRequest(
    @NotNull UUID distillationBatchId,
    @NotNull UUID brandId,
    @NotNull UUID categoryId,
    @NotNull LocalDate bottlingDate,
    @NotNull @Positive Integer bottleCapacityMl,
    @NotNull @Positive BigDecimal totalVolumeL,
    @NotNull @NotBlank String productionLotNumber,
    @NotNull @PositiveOrZero Integer unitsBottled,
    @PositiveOrZero Integer registeredLossesUnits,
    @NotNull @NotEmpty List<UUID> assignedTaxLabelIds,
    @NotNull Map<UUID, String> labelValues,
    String notes
) {
}
