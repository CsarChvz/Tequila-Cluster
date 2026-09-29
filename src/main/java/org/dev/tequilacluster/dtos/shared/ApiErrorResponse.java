package org.dev.tequilacluster.dtos.shared;

import java.time.Instant;

/** Standard JSON error payload for every non-2xx response (NFR-13). */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path);
    }
}
