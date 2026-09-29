package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04/FR-28/FR-29: tipos de embarque y sus requisitos documentales. */
public record ShipmentTypeRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
