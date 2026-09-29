package org.dev.tequilacluster.dtos.quality;

import jakarta.validation.constraints.NotNull;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;

/** FR-33: Request to update the status of an existing non-conformity. */
public record NonConformityStatusUpdateRequest(
        @NotNull(message = "Status is required")
        NonConformityStatus status
) {
}
