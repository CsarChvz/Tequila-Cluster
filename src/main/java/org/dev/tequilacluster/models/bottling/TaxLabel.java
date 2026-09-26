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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.bottling.enums.TaxLabelStatus;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: tax_label. Marbetes fiscales (SAT), folio único (FR-22). */
@Entity
@Table(name = "tax_label")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "folio", nullable = false, unique = true, length = 100)
    private String folio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaxLabelStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottling_batch_id")
    private BottlingBatch bottlingBatch;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "used_at")
    private Instant usedAt;
}
