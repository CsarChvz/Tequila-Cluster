package org.dev.tequilacluster.dtos.logistics;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** FR-26 a FR-28: solicitud de creación de embarque en estado PLANNED. */
public record ShipmentCreateRequest(
        @NotBlank @Size(max = 80) String shipmentNumber,
        @NotNull UUID shipmentTypeId,
        @NotNull UUID carrierId,
        @NotBlank @Size(max = 30) String vehicleLicensePlate,
        @NotBlank String destination,
        @NotNull Instant departureAt,
        @NotNull Instant estimatedArrivalAt,
        @NotEmpty List<@Valid ShipmentItemRequest> items
) {
}
