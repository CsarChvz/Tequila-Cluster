package org.dev.tequilacluster.dtos.catalogs;

import jakarta.validation.constraints.NotBlank;

/** FR-04: Administrator-managed supplier catalog. */
public record SupplierRequest(
        @NotBlank String supplierCode,
        @NotBlank String legalName,
        String taxId,
        String contactName,
        String phone,
        String email
) {
}
