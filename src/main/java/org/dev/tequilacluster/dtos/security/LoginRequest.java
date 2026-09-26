package org.dev.tequilacluster.dtos.security;

import jakarta.validation.constraints.NotBlank;

/** FR-01 login payload. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
