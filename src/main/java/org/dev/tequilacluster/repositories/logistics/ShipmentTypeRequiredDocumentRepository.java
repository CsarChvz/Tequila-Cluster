package org.dev.tequilacluster.repositories.logistics;

import org.dev.tequilacluster.models.logistics.ShipmentTypeRequiredDocument;
import org.dev.tequilacluster.models.logistics.ShipmentTypeRequiredDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-29/RB-402: documentos requeridos configurados por tipo de embarque. */
public interface ShipmentTypeRequiredDocumentRepository
        extends JpaRepository<ShipmentTypeRequiredDocument, ShipmentTypeRequiredDocumentId> {

    List<ShipmentTypeRequiredDocument> findByShipmentTypeId(UUID shipmentTypeId);

    List<ShipmentTypeRequiredDocument> findByShipmentTypeIdAndRequiredTrue(UUID shipmentTypeId);
}
