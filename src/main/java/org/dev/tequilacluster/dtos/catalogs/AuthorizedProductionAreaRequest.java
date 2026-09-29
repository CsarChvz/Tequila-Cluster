package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/** FR-04/RB-101: catálogo de áreas de producción autorizadas (Denominación de Origen). */
public record AuthorizedProductionAreaRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String stateName,
        String municipality,
        LocalDate validFrom,
        LocalDate validTo
) {
}
