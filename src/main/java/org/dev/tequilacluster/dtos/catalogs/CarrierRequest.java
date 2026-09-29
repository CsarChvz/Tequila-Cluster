package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04/FR-28: transportistas encargados de mover producto embarcado. */
public record CarrierRequest(
        @NotBlank String name,
        String taxId,
        String phone,
        String email
) {
}
