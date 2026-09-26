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

/** Clave compuesta embebida de shipment_type_required_document (shipment_type_id, document_type_id). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShipmentTypeRequiredDocumentId implements Serializable {

    @Column(name = "shipment_type_id")
    private UUID shipmentTypeId;

    @Column(name = "document_type_id")
    private UUID documentTypeId;
}
