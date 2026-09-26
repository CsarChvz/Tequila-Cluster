package org.dev.tequilacluster.models.quality;

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
import org.dev.tequilacluster.models.bottling.BottledUnit;

/** Tabla origen: recall_unit. Unidades individuales incluidas en un recall parcial (FR-34). */
@Entity
@Table(name = "recall_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecallUnit {

    @EmbeddedId
    private RecallUnitId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recallId")
    @JoinColumn(name = "recall_id", nullable = false)
    private Recall recall;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bottledUnitId")
    @JoinColumn(name = "bottled_unit_id", nullable = false)
    private BottledUnit bottledUnit;
}
