package org.dev.tequilacluster.models.distillation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.shared.Batch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tabla origen: distillation_batch. Detalle de un lote de Destilación; su PK es también FK hacia
 * batch(id) (FR-14 a FR-19).
 */
@Entity
@Table(name = "distillation_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistillationBatch {

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @Column(name = "distillation_date", nullable = false)
    private LocalDate distillationDate;

    @Column(name = "total_distilled_volume_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal totalDistilledVolumeL;

    @Column(name = "heads_volume_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal headsVolumeL;

    @Column(name = "hearts_volume_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal heartsVolumeL;

    @Column(name = "tails_volume_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal tailsVolumeL;

    @Column(name = "alcohol_content_pct", nullable = false, precision = 6, scale = 3)
    private BigDecimal alcoholContentPct;

    @Column(name = "cooking_temperature_c", precision = 7, scale = 3)
    private BigDecimal cookingTemperatureC;

    @Column(name = "fermentation_ph", precision = 5, scale = 3)
    private BigDecimal fermentationPh;

    @Column(name = "actual_yield_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal actualYieldL;

    @Column(name = "estimated_yield_l_snapshot", nullable = false, precision = 14, scale = 3)
    private BigDecimal estimatedYieldLSnapshot;

    @Column(name = "maturation_required", nullable = false)
    private Boolean maturationRequired;

    @Column(name = "maturation_start_date")
    private LocalDate maturationStartDate;

    @Column(name = "required_maturation_days", nullable = false)
    private Integer requiredMaturationDays;

    @Column(name = "ready_for_bottling_at")
    private Instant readyForBottlingAt;

    @Column(name = "notes")
    private String notes;
}
