package org.dev.tequilacluster.models.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Clave compuesta embebida de recall_unit (recall_id, bottled_unit_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RecallUnitId implements Serializable {

    @Column(name = "recall_id")
    private UUID recallId;

    @Column(name = "bottled_unit_id")
    private UUID bottledUnitId;
}
