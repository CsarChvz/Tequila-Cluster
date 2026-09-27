package org.dev.tequilacluster.dtos.bottling;

import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** FR-20 to FR-25 response: bottling batch details. */
public record BottlingBatchResponse(
    UUID batchId,
    String traceabilityCode,
    BatchStatus status,
    String brandName,
    String categoryName,
    LocalDate bottlingDate,
    Integer bottleCapacityMl,
    BigDecimal totalVolumeL,
    String productionLotNumber,
    Integer unitsBottled,
    Integer registeredLossesUnits,
    Integer taxLabelsAssigned,
    Integer bottledUnitsCreated,
    String sourceDistillationTraceabilityCode,
    String notes,
    boolean taxLabelReconciliationWarning
) {
}
