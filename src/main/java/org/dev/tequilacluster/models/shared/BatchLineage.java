package org.dev.tequilacluster.models.shared;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.security.AppUser;

import java.math.BigDecimal;
import java.time.Instant;

/** Tabla origen: batch_lineage. Genealogía padre-hijo entre lotes (RB-004, FR-37, FR-38). */
@Entity
@Table(name = "batch_lineage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchLineage {

    @EmbeddedId
    private BatchLineageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("parentBatchId")
    @JoinColumn(name = "parent_batch_id", nullable = false)
    private Batch parentBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("childBatchId")
    @JoinColumn(name = "child_batch_id", nullable = false)
    private Batch childBatch;

    @Column(name = "quantity_used", precision = 18, scale = 6)
    private BigDecimal quantityUsed;

    @Column(name = "unit", length = 20)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_by")
    private AppUser linkedBy;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;
}
