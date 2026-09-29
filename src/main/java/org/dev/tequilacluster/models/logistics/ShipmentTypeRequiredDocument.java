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
import org.dev.tequilacluster.models.catalogs.DocumentType;
import org.dev.tequilacluster.models.catalogs.ShipmentType;

/** Tabla origen: shipment_type_required_document. Documentos obligatorios según tipo de embarque (FR-29). */
@Entity
@Table(name = "shipment_type_required_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentTypeRequiredDocument {

    @EmbeddedId
    private ShipmentTypeRequiredDocumentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("shipmentTypeId")
    @JoinColumn(name = "shipment_type_id", nullable = false)
    private ShipmentType shipmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentTypeId")
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Column(name = "required", nullable = false)
    private Boolean required;
}
