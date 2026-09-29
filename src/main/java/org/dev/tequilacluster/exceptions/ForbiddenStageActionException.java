package org.dev.tequilacluster.exceptions;

/**
 * The authenticated user's role does not have the required permission
 * (view/create/update/complete) on the given process stage — RB-504. Maps to HTTP 403.
 */
public class ForbiddenStageActionException extends RuntimeException {

    public ForbiddenStageActionException(String stageCode, String action) {
        super("Role has no '" + action + "' permission on stage " + stageCode);
    }
}
