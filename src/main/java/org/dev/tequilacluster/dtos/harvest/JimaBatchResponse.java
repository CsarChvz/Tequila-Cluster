package org.dev.tequilacluster.dtos.harvest;

import org.dev.tequilacluster.models.shared.enums.BatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** FR-06 to FR-11 response: harvest batch plus its auto-generated transport permit. */
public record JimaBatchResponse(
        UUID batchId,
        String traceabilityCode,
        BatchStatus status,
        String fieldCode,
        String supplierCode,
        LocalDate harvestDate,
        BigDecimal totalWeightKg,
        Integer agaveHeartsCount,
        BigDecimal estimatedYieldL,
        BigDecimal estimatedYieldFactor,
        String transportPermitNumber,
        boolean capacityWarning
) {
}
