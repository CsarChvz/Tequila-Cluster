package org.dev.tequilacluster.models.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.dev.tequilacluster.models.security.AppUser;

import java.time.Instant;
import java.util.UUID;

/** Tabla origen: audit_log. Bitácora general de auditoría append-only (RB-003, FR-42). */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "before_data", columnDefinition = "jsonb")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "jsonb")
    private String afterData;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
