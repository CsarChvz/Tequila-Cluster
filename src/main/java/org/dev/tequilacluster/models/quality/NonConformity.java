package org.dev.tequilacluster.models.quality;

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
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.quality.enums.NonConformitySeverity;
import org.dev.tequilacluster.models.quality.enums.NonConformityStatus;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: non_conformity. Incidencias de calidad sobre un batch o unidad embotellada (FR-33). */
@Entity
@Table(name = "non_conformity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonConformity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottled_unit_id")
    private BottledUnit bottledUnit;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private NonConformitySeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NonConformityStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private AppUser reportedBy;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
