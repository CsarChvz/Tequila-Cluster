package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record CarrierResponse(
        UUID id,
        String name,
        String taxId,
        String phone,
        String email,
        boolean active
) {
}
