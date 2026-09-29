package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record TequilaCategoryResponse(
        UUID id,
        String code,
        String name,
        Integer minimumMaturationDays,
        boolean active
) {
}
