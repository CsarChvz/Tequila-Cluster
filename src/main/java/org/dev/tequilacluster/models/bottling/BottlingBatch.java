package org.dev.tequilacluster.models.bottling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.catalogs.Brand;
import org.dev.tequilacluster.models.catalogs.TequilaCategory;
import org.dev.tequilacluster.models.shared.Batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tabla origen: bottling_batch. Detalle de un lote de Envasado; su PK es también FK hacia
 * batch(id) (FR-20, FR-21).
 */
@Entity
@Table(name = "bottling_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BottlingBatch {

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TequilaCategory category;

    @Column(name = "bottling_date", nullable = false)
    private LocalDate bottlingDate;

    @Column(name = "bottle_capacity_ml", nullable = false)
    private Integer bottleCapacityMl;

    @Column(name = "total_volume_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal totalVolumeL;

    @Column(name = "production_lot_number", nullable = false, unique = true, length = 80)
    private String productionLotNumber;

    @Column(name = "units_bottled", nullable = false)
    private Integer unitsBottled;

    @Column(name = "registered_losses_units", nullable = false)
    private Integer registeredLossesUnits;

    @Column(name = "notes")
    private String notes;
}
