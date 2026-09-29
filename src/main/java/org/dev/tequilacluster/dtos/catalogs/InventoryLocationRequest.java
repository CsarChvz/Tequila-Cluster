package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04/FR-35: ubicaciones físicas de inventario. */
public record InventoryLocationRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
