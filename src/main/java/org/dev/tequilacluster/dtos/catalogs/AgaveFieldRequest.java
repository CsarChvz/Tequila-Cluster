package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/** FR-04/FR-06: catálogo de predios de agave, ligados a un área de producción autorizada. */
public record AgaveFieldRequest(
        @NotBlank String fieldCode,
        @NotBlank String name,
        @NotNull UUID authorizedAreaId,
        String addressText,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
