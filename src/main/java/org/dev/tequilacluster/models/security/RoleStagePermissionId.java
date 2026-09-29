package org.dev.tequilacluster.models.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/** Clave compuesta embebida de role_stage_permission (role_id, stage_code). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleStagePermissionId implements Serializable {

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "stage_code", length = 30)
    private String stageCode;
}
