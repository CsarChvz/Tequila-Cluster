package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04/FR-21: marcas comerciales usadas en el envasado. */
public record BrandRequest(
        @NotBlank String name
) {
}
