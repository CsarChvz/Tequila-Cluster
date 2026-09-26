package org.dev.tequilacluster.models.bottling;

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
import org.dev.tequilacluster.models.catalogs.LabelRequirement;

/** Tabla origen: bottling_label_value. Valores de etiquetado capturados por lote de envasado (FR-24). */
@Entity
@Table(name = "bottling_label_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BottlingLabelValue {

    @EmbeddedId
    private BottlingLabelValueId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bottlingBatchId")
    @JoinColumn(name = "bottling_batch_id", nullable = false)
    private BottlingBatch bottlingBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("requirementId")
    @JoinColumn(name = "requirement_id", nullable = false)
    private LabelRequirement requirement;

    @Column(name = "value_text", nullable = false)
    private String valueText;
}
