package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * FR-43: Detailed jima stage view for batch history.
 */
public record JimaBatchDetailDto(
        UUID fieldId,
        String fieldCode,
        String fieldName,
        UUID supplierId,
        String supplierName,
        String supplierTaxId,
        UUID authorizedAreaId,
        String authorizedAreaCode,
        String authorizedAreaName,
        LocalDate harvestDate,
        BigDecimal totalWeightKg,
        Integer agaveHeartsCount,
        BigDecimal estimatedYieldL,
        BigDecimal estimatedYieldFactor,
        String notes
) {}
