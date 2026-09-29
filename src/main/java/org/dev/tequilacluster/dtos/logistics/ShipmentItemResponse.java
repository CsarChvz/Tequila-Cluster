package org.dev.tequilacluster.dtos.logistics;

import java.util.UUID;

/** FR-26/27: detalle de item de embarque en respuesta. */
public record ShipmentItemResponse(
        UUID bottlingBatchId,
        String productionLotNumber,
        String traceabilityCode,
        Integer quantityUnits
) {
}
