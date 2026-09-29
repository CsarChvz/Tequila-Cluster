package org.dev.tequilacluster.dtos.catalogs;

import java.math.BigDecimal;
import java.util.UUID;

public record AgaveFieldResponse(
        UUID id,
        String fieldCode,
        String name,
        UUID authorizedAreaId,
        String authorizedAreaCode,
        String addressText,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean active
) {
}
