package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04/FR-29: catálogo de documentos que pueden acompañar un embarque. */
public record DocumentTypeRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
