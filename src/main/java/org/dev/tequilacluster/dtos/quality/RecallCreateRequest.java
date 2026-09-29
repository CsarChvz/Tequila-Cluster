package org.dev.tequilacluster.dtos.quality;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.dev.tequilacluster.models.quality.enums.RecallType;

import java.util.List;
import java.util.UUID;

/** FR-34: Request to register a new product recall (partial or complete). */
public record RecallCreateRequest(
        @NotNull(message = "Source batch ID is required")
        UUID sourceBatchId,

        UUID nonConformityId,

        @NotNull(message = "Recall type is required")
        RecallType recallType,

        @NotBlank(message = "Reason is required")
        String reason,

        List<UUID> bottledUnitIds
) {
}
