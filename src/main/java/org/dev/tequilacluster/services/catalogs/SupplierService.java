package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.SupplierRequest;
import org.dev.tequilacluster.dtos.catalogs.SupplierResponse;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.Supplier;
import org.dev.tequilacluster.repositories.catalogs.SupplierRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * FR-04: Administrator CRUD for the supplier catalog (RB-102 depends on {@code active}).
 * Reference implementation for the other 10 simple catalogs (authorized_production_area,
 * agave_field, brand, tequila_category, carrier, validation_rule, shipment_type, document_type,
 * label_requirement, inventory_location) — see the TODO report, they follow this same shape.
 */
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> list() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SupplierResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        applyRequest(supplier, request);
        supplier.setActive(true);
        supplier.setCreatedAt(Instant.now());
        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(UUID id, SupplierRequest request) {
        Supplier supplier = findOrThrow(id);
        applyRequest(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        Supplier supplier = findOrThrow(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private Supplier findOrThrow(UUID id) {
        return supplierRepository.findById(id).orElseThrow(() -> NotFoundException.of("Supplier", id));
    }

    private void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setSupplierCode(request.supplierCode());
        supplier.setLegalName(request.legalName());
        supplier.setTaxId(request.taxId());
        supplier.setContactName(request.contactName());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getLegalName(),
                supplier.getTaxId(),
                supplier.getContactName(),
                supplier.getPhone(),
                supplier.getEmail(),
                Boolean.TRUE.equals(supplier.getActive())
        );
    }
}
