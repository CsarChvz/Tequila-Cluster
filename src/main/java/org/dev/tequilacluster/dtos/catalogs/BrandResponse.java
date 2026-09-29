package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name,
        boolean active
) {
}
