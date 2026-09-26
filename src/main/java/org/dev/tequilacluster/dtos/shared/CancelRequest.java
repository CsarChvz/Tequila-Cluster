package org.dev.tequilacluster.dtos.shared;

import jakarta.validation.constraints.NotBlank;

/** RB-503 / FR-40: cancelling a batch always requires a reason. */
public record CancelRequest(@NotBlank String reason) {
}
