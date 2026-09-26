package org.dev.tequilacluster.models.logistics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Clave compuesta embebida de shipment_item (shipment_id, bottling_batch_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShipmentItemId implements Serializable {

    @Column(name = "shipment_id")
    private UUID shipmentId;

    @Column(name = "bottling_batch_id")
    private UUID bottlingBatchId;
}
