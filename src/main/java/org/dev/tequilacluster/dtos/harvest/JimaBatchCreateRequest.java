package org.dev.tequilacluster.dtos.harvest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** FR-06: mandatory harvest batch fields. */
public record JimaBatchCreateRequest(
        @NotNull UUID fieldId,
        @NotNull UUID supplierId,
        @NotNull LocalDate harvestDate,
        @NotNull @Positive BigDecimal totalWeightKg,
        @NotNull @Positive Integer agaveHeartsCount,
        String notes
) {
}
