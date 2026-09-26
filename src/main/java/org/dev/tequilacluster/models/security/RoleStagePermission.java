package org.dev.tequilacluster.models.security;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.shared.ProcessStage;

/** Tabla origen: role_stage_permission. Permisos view/create/update/complete por rol+etapa (FR-03). */
@Entity
@Table(name = "role_stage_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleStagePermission {

    @EmbeddedId
    private RoleStagePermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stageCode")
    @JoinColumn(name = "stage_code", nullable = false)
    private ProcessStage processStage;

    @Column(name = "can_view", nullable = false)
    private Boolean canView;

    @Column(name = "can_create", nullable = false)
    private Boolean canCreate;

    @Column(name = "can_update", nullable = false)
    private Boolean canUpdate;

    @Column(name = "can_complete", nullable = false)
    private Boolean canComplete;
}
