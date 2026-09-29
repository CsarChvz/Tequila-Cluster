package org.dev.tequilacluster.dtos.catalogs;

import java.time.LocalDate;
import java.util.UUID;

public record AuthorizedProductionAreaResponse(
        UUID id,
        String code,
        String name,
        String stateName,
        String municipality,
        boolean active,
        LocalDate validFrom,
        LocalDate validTo
) {
}
