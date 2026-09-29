package org.dev.tequilacluster.dtos.logistics;

import java.util.List;
import java.util.UUID;

/**
 * FR-31 / RB-404: Resultado del chequeo de alertas por retraso en envíos.
 */
public record ShipmentDelayCheckResponse(
        int inTransitShipmentsEvaluated,
        int delayedShipmentsDetected,
        int newAlertsCreated,
        long toleranceHoursUsed,
        List<ShipmentDelayAlertItem> alerts
) {
    public record ShipmentDelayAlertItem(
            UUID alertId,
            UUID shipmentId,
            String shipmentNumber,
            String message,
            boolean isNew
    ) {}
}
