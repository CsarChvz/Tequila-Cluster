package org.dev.tequilacluster.dtos.distillation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * TODO(dev-a): shape this to match FR-13/FR-14/FR-19 once the service is implemented — this is
 * a starting point, not a final contract. {@code sourceHarvestBatches} feeds RB-201
 * (batch_lineage: which harvest batches, how much of each, in what unit).
 */
public record DistillationBatchCreateRequest(
        List<HarvestLineageItem> sourceHarvestBatches,
        LocalDate distillationDate,
        BigDecimal totalDistilledVolumeL,
        BigDecimal headsVolumeL,
        BigDecimal heartsVolumeL,
        BigDecimal tailsVolumeL,
        BigDecimal alcoholContentPct,
        BigDecimal cookingTemperatureC,
        BigDecimal fermentationPh,
        BigDecimal actualYieldL,
        boolean maturationRequired,
        LocalDate maturationStartDate,
        Integer requiredMaturationDays,
        String notes
) {
    public record HarvestLineageItem(UUID harvestBatchId, BigDecimal quantityUsed, String unit) {
    }
}
