package org.dev.tequilacluster.dtos.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** FR-01/FR-02: self-service operator registration. Role assignment excludes ADMINISTRATOR. */
public record RegisterRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 6, max = 255) String password,
        @NotBlank String roleCode
) {
}
