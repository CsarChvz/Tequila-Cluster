package org.dev.tequilacluster.models.harvest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.harvest.enums.TransportPermitStatus;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: transport_permit. Permiso de traslado generado automáticamente por cada jima_batch (FR-10). */
@Entity
@Table(name = "transport_permit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportPermit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jima_batch_id", nullable = false, unique = true)
    private JimaBatch jimaBatch;

    @Column(name = "permit_number", nullable = false, unique = true, length = 80)
    private String permitNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransportPermitStatus status;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "document_url")
    private String documentUrl;
}
