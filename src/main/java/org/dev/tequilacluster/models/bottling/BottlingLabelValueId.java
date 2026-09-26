package org.dev.tequilacluster.models.bottling;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Clave compuesta embebida de bottling_label_value (bottling_batch_id, requirement_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BottlingLabelValueId implements Serializable {

    @Column(name = "bottling_batch_id")
    private UUID bottlingBatchId;

    @Column(name = "requirement_id")
    private UUID requirementId;
}
