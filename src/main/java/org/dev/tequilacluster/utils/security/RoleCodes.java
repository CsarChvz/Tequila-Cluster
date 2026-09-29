package org.dev.tequilacluster.utils.security;

/**
 * Expected {@code role.code} values (FR-02). Seeded by
 * {@code src/main/resources/sql/seed_reference_data.sql}; update these constants if an
 * environment seeds different codes.
 */
public final class RoleCodes {

    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String JIMA_OPERATOR = "JIMA_OPERATOR";
    public static final String DISTILLATION_OPERATOR = "DISTILLATION_OPERATOR";
    public static final String BOTTLING_OPERATOR = "BOTTLING_OPERATOR";
    public static final String LOGISTICS_OPERATOR = "LOGISTICS_OPERATOR";
    public static final String AUDITOR = "AUDITOR";

    private RoleCodes() {
    }
}
