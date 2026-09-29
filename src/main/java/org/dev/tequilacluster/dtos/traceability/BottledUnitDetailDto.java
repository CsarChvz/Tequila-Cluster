package org.dev.tequilacluster.dtos.traceability;

import org.dev.tequilacluster.models.bottling.enums.BarcodeType;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;

import java.util.UUID;

/**
 * Detail of bottled unit when querying traceability by unitCode.
 */
public record BottledUnitDetailDto(
        UUID id,
        String unitCode,
        BarcodeType barcodeType,
        BottledUnitStatus status,
        UUID taxLabelId,
        String taxLabelFolio,
        UUID bottlingBatchId,
        String productionLotNumber
) {}
