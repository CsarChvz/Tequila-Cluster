package org.dev.tequilacluster.models.logistics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.catalogs.Carrier;
import org.dev.tequilacluster.models.catalogs.ShipmentType;
import org.dev.tequilacluster.models.logistics.enums.ShipmentStatus;
import org.dev.tequilacluster.models.security.AppUser;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: shipment. Embarque o traslado de producto terminado (FR-26 a FR-32). */
@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "shipment_number", nullable = false, unique = true, length = 80)
    private String shipmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_type_id", nullable = false)
    private ShipmentType shipmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @Column(name = "vehicle_license_plate", nullable = false, length = 30)
    private String vehicleLicensePlate;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "departure_at", nullable = false)
    private Instant departureAt;

    @Column(name = "estimated_arrival_at", nullable = false)
    private Instant estimatedArrivalAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ShipmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
