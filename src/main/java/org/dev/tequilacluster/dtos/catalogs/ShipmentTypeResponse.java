package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record ShipmentTypeResponse(
        UUID id,
        String code,
        String name,
        boolean active
) {
}
