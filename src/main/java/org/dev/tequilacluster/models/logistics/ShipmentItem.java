package org.dev.tequilacluster.models.logistics;

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
import org.dev.tequilacluster.models.bottling.BottlingBatch;

/** Tabla origen: shipment_item. Detalle de lotes de envasado incluidos en un embarque (FR-26, FR-27, FR-30). */
@Entity
@Table(name = "shipment_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentItem {

    @EmbeddedId
    private ShipmentItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("shipmentId")
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bottlingBatchId")
    @JoinColumn(name = "bottling_batch_id", nullable = false)
    private BottlingBatch bottlingBatch;

    @Column(name = "quantity_units", nullable = false)
    private Integer quantityUnits;
}
