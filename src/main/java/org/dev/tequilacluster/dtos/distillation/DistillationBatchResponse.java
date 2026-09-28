package org.dev.tequilacluster.dtos.distillation;

import org.dev.tequilacluster.models.shared.enums.BatchStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DistillationBatchResponse(
    UUID batchId,
    String traceabilityCode,
    BatchStatus status,
    LocalDate distillationDate,
    BigDecimal totalDistilledVolumeL,
    BigDecimal headsVolumeL,
    BigDecimal heartsVolumeL,
    BigDecimal tailsVolumeL,
    BigDecimal alcoholContentPct,
    BigDecimal cookingTemperatureC,
    BigDecimal fermentationPh,
    BigDecimal actualYieldL,
    BigDecimal estimatedYieldLSnapshot,
    boolean maturationRequired,
    LocalDate maturationStartDate,
    Integer requiredMaturationDays,
    Instant readyForBottlingAt,
    String notes,
    List<String> sourceTraceabilityCodes
) {}
