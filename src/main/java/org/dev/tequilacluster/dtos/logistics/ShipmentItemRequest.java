package org.dev.tequilacluster.dtos.logistics;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/** FR-26/27: item de embarque con lote de envasado y cantidad solicitada. */
public record ShipmentItemRequest(
        @NotNull UUID bottlingBatchId,
        @NotNull @Positive Integer quantityUnits
) {
}
