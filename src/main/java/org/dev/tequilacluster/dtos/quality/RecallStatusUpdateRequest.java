package org.dev.tequilacluster.dtos.quality;

import jakarta.validation.constraints.NotNull;
import org.dev.tequilacluster.models.quality.enums.RecallStatus;

/** FR-34: Request to update the status of an existing recall. */
public record RecallStatusUpdateRequest(
        @NotNull(message = "Status is required")
        RecallStatus status,

        String reason
) {
}
