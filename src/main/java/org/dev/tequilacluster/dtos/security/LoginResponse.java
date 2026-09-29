package org.dev.tequilacluster.dtos.security;

import java.util.List;
import java.util.UUID;

/** FR-01 login response: token plus enough identity data for the frontend to render permissions. */
public record LoginResponse(
        String token,
        UUID userId,
        String username,
        List<String> roles
) {
}
