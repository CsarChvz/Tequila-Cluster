package org.dev.tequilacluster.dtos.logistics;

import java.time.Instant;
import java.util.UUID;

/** FR-29: detalle de documento de embarque en respuesta. */
public record ShipmentDocumentResponse(
        UUID id,
        UUID documentTypeId,
        String documentTypeCode,
        String documentTypeName,
        String documentNumber,
        String fileUrl,
        Boolean valid,
        Instant uploadedAt,
        UUID uploadedById,
        String uploadedByUsername
) {
}
