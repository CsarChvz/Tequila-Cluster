package org.dev.tequilacluster.dtos.quality;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;

import java.util.UUID;

/** FR-33: Request to register a new non-conformity on a batch (and optionally a bottled unit). */
public record NonConformityCreateRequest(
        @NotNull(message = "Batch ID is required")
        UUID batchId,

        UUID bottledUnitId,

        @NotBlank(message = "Title is required")
        @Size(max = 180, message = "Title cannot exceed 180 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Severity is required")
        NonConformitySeverity severity
) {
}
