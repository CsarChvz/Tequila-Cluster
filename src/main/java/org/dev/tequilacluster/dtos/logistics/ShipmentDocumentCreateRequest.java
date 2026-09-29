package org.dev.tequilacluster.dtos.logistics;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** FR-29: registro de un documento de embarque. */
public record ShipmentDocumentCreateRequest(
        @NotNull UUID documentTypeId,
        @Size(max = 120) String documentNumber,
        String fileUrl,
        Boolean valid
) {
}
