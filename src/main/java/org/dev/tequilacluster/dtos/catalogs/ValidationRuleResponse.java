package org.dev.tequilacluster.dtos.catalogs;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ValidationRuleResponse(
        UUID id,
        String stageCode,
        String parameterCode,
        String displayName,
        String unit,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal allowedDeviation,
        boolean active,
        Instant validFrom,
        Instant validTo
) {
}
