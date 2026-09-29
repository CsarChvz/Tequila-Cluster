package org.dev.tequilacluster.exceptions;

/** Entity or record requested by id/code does not exist. Maps to HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entityName, Object id) {
        return new NotFoundException(entityName + " not found: " + id);
    }
}
