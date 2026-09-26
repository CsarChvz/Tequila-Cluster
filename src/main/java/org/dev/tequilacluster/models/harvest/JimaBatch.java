package org.dev.tequilacluster.models.harvest;

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
import org.dev.tequilacluster.models.catalogs.AgaveField;
import org.dev.tequilacluster.models.catalogs.Supplier;
import org.dev.tequilacluster.models.shared.Batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tabla origen: jima_batch. Detalle de un lote de cosecha (Jima); su PK es también FK hacia
 * batch(id) (FR-06, FR-07, FR-08, FR-09).
 */
@Entity
@Table(name = "jima_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JimaBatch {

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private AgaveField field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "total_weight_kg", nullable = false, precision = 14, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "agave_hearts_count", nullable = false)
    private Integer agaveHeartsCount;

    @Column(name = "estimated_yield_l", nullable = false, precision = 14, scale = 3)
    private BigDecimal estimatedYieldL;

    @Column(name = "estimated_yield_factor", nullable = false, precision = 12, scale = 6)
    private BigDecimal estimatedYieldFactor;

    @Column(name = "notes")
    private String notes;
}
