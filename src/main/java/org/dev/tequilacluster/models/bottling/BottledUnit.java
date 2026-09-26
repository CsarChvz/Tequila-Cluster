package org.dev.tequilacluster.models.bottling;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.bottling.enums.BarcodeType;
import org.dev.tequilacluster.models.bottling.enums.BottledUnitStatus;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: bottled_unit. Unidad física individual (botella) identificada por QR/barcode (FR-23). */
@Entity
@Table(name = "bottled_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BottledUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottling_batch_id", nullable = false)
    private BottlingBatch bottlingBatch;

    @Column(name = "unit_code", nullable = false, unique = true, length = 100)
    private String unitCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", nullable = false, length = 20)
    private BarcodeType barcodeType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_label_id", unique = true)
    private TaxLabel taxLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BottledUnitStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
