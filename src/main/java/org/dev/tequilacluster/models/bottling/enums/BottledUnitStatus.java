package org.dev.tequilacluster.models.bottling.enums;

/** Valores permitidos para bottled_unit.status (CHECK de la tabla bottled_unit). */
public enum BottledUnitStatus {
    AVAILABLE,
    RESERVED,
    SHIPPED,
    DELIVERED,
    RECALLED,
    LOST,
    DAMAGED
}
