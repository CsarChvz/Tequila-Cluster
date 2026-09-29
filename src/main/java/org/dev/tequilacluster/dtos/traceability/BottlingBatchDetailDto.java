package org.dev.tequilacluster.dtos.traceability;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * FR-43: Detailed bottling stage view for batch history.
 */
public record BottlingBatchDetailDto(
        UUID brandId,
        String brandName,
        UUID categoryId,
        String categoryName,
        LocalDate bottlingDate,
        Integer bottleCapacityMl,
        BigDecimal totalVolumeL,
        String productionLotNumber,
        Integer unitsBottled,
        Integer registeredLossesUnits,
        String notes
) {}
