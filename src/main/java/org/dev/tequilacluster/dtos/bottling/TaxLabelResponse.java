package org.dev.tequilacluster.dtos.bottling;

import org.dev.tequilacluster.models.bottling.enums.TaxLabelStatus;

import java.util.UUID;

/** FR-22: tax label (marbete) summary for the bottling batch creation form. */
public record TaxLabelResponse(
        UUID id,
        String folio,
        TaxLabelStatus status
) {
}
