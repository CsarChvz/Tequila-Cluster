package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.CarrierRequest;
import org.dev.tequilacluster.dtos.catalogs.CarrierResponse;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.Carrier;
import org.dev.tequilacluster.repositories.catalogs.CarrierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** FR-04/FR-28: Administrator CRUD for carriers. */
@Service
public class CarrierService {

    private final CarrierRepository repository;

    public CarrierService(CarrierRepository repository) {
        this.repository = repository;
    }

    public List<CarrierResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CarrierResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public CarrierResponse create(CarrierRequest request) {
        Carrier carrier = new Carrier();
        applyRequest(carrier, request);
        carrier.setActive(true);
        return toResponse(repository.save(carrier));
    }

    public CarrierResponse update(UUID id, CarrierRequest request) {
        Carrier carrier = findOrThrow(id);
        applyRequest(carrier, request);
        return toResponse(repository.save(carrier));
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        Carrier carrier = findOrThrow(id);
        carrier.setActive(false);
        repository.save(carrier);
    }

    private Carrier findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Carrier", id));
    }

    private void applyRequest(Carrier carrier, CarrierRequest request) {
        carrier.setName(request.name());
        carrier.setTaxId(request.taxId());
        carrier.setPhone(request.phone());
        carrier.setEmail(request.email());
    }

    private CarrierResponse toResponse(Carrier carrier) {
        return new CarrierResponse(
                carrier.getId(),
                carrier.getName(),
                carrier.getTaxId(),
                carrier.getPhone(),
                carrier.getEmail(),
                Boolean.TRUE.equals(carrier.getActive())
        );
    }
}
