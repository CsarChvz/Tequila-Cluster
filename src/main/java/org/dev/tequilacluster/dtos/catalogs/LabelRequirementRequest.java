package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** FR-04/FR-24/RB-305/307: datos obligatorios/configurables del etiquetado. */
public record LabelRequirementRequest(
        @NotBlank String code,
        @NotBlank String displayName,
        @NotNull Boolean required
) {
}
