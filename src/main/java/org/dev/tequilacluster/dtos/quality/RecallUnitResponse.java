package org.dev.tequilacluster.dtos.quality;

import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;

import java.util.UUID;

/** FR-34: Detailed response for an individual bottled unit in a recall. */
public record RecallUnitResponse(
        UUID recallId,
        UUID bottledUnitId,
        String unitCode,
        BottledUnitStatus bottleStatus
) {
}
