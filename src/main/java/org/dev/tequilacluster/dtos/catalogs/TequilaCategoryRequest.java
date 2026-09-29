package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** FR-04/RB-205/RB-206: categoría comercial de tequila y su maduración mínima. */
public record TequilaCategoryRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull @Min(0) Integer minimumMaturationDays
) {
}
