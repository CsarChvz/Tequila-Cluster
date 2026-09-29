package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Agricultural origin details (Harvest/Jima, Field, Supplier, Authorized Area).
 */
public record HarvestOriginDto(
        UUID batchId,
        String traceabilityCode,
        LocalDate harvestDate,
        BigDecimal totalWeightKg,
        Integer agaveHeartsCount,
        BigDecimal estimatedYieldL,
        BigDecimal estimatedYieldFactor,
        UUID fieldId,
        String fieldCode,
        String fieldName,
        UUID supplierId,
        String supplierName,
        String supplierTaxId,
        UUID authorizedAreaId,
        String authorizedAreaCode,
        String authorizedAreaName,
        String stateName,
        String municipality
) {}
