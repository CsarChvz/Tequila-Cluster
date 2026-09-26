package org.dev.tequilacluster.models.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Clave compuesta embebida de batch_lineage (parent_batch_id, child_batch_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BatchLineageId implements Serializable {

    @Column(name = "parent_batch_id")
    private UUID parentBatchId;

    @Column(name = "child_batch_id")
    private UUID childBatchId;
}
