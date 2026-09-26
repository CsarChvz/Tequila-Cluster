package org.dev.tequilacluster.utils.shared;

/**
 * Expected {@code process_stage.code} values (FR-02, NFR-15). The DDL does not seed this
 * catalog — see {@code src/main/resources/sql/seed_reference_data.sql}, applied by
 * {@code compose.yml} on first container start. If an environment seeds different codes,
 * update these constants to match.
 */
public final class ProcessStageCodes {

    public static final String HARVEST = "HARVEST";
    public static final String DISTILLATION = "DISTILLATION";
    public static final String BOTTLING = "BOTTLING";
    public static final String LOGISTICS = "LOGISTICS";

    private ProcessStageCodes() {
    }
}
