package org.dev.tequilacluster.exceptions;

/**
 * A domain/business rule (RB-*) was violated, e.g. RB-202 (cuts exceed distilled volume) or
 * RB-101 (agave field outside an active authorized production area). Maps to HTTP 422.
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String ruleCode;

    public BusinessRuleViolationException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
