package org.dev.tequilacluster.repositories.logistics;

import org.dev.tequilacluster.models.logistics.ShipmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-29/RB-402: documentos cargados por embarque; validez requerida antes de salir de PLANNED. */
public interface ShipmentDocumentRepository extends JpaRepository<ShipmentDocument, UUID> {

    List<ShipmentDocument> findByShipmentId(UUID shipmentId);

    List<ShipmentDocument> findByShipmentIdAndValidTrue(UUID shipmentId);

    List<ShipmentDocument> findByDocumentTypeId(UUID documentTypeId);
}
