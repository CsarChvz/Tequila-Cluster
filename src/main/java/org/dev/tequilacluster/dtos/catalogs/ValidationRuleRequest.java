package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/** FR-04/RB-103/203/204/207: reglas configurables de rango/tolerancia por etapa+parámetro. */
public record ValidationRuleRequest(
        @NotBlank String stageCode,
        @NotBlank String parameterCode,
        @NotBlank String displayName,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal allowedDeviation,
        @NotNull Instant validFrom,
        Instant validTo
) {
}
