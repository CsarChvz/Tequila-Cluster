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
import org.dev.tequilacluster.models.quality.enums.RecallStatus;
import org.dev.tequilacluster.models.quality.enums.RecallType;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: recall. Proceso formal de retiro de producto (FR-34). */
@Entity
@Table(name = "recall")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recall {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_conformity_id")
    private NonConformity nonConformity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_batch_id", nullable = false)
    private Batch sourceBatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "recall_type", nullable = false, length = 20)
    private RecallType recallType;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecallStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by")
    private AppUser startedBy;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
