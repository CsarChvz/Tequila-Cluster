package org.dev.tequilacluster.repositories.logistics;

import org.dev.tequilacluster.models.logistics.ShipmentItem;
import org.dev.tequilacluster.models.logistics.ShipmentItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-26/27/30: detalle de lotes de envasado incluidos por embarque; un batch puede repartirse en varios envíos. */
public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, ShipmentItemId> {

    List<ShipmentItem> findByShipmentId(UUID shipmentId);

    List<ShipmentItem> findByBottlingBatchId(UUID bottlingBatchId);
}
