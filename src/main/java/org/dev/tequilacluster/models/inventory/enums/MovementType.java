package org.dev.tequilacluster.models.inventory.enums;

/** Valores permitidos para inventory_movement.movement_type (CHECK de la tabla inventory_movement). */
public enum MovementType {
    INITIAL,
    PRODUCTION_IN,
    RETURN_IN,
    SHIPMENT_OUT,
    LOSS_OUT,
    RECALL_OUT,
    ADJUSTMENT
}
