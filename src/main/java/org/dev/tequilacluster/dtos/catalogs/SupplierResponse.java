package org.dev.tequilacluster.dtos.catalogs;

import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String supplierCode,
        String legalName,
        String taxId,
        String contactName,
        String phone,
        String email,
        boolean active
) {
}
