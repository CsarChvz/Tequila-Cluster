package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record LabelRequirementResponse(
        UUID id,
        String code,
        String displayName,
        boolean required,
        boolean active
) {
}
