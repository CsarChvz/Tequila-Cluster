package org.dev.tequilacluster.repositories.security;

import org.dev.tequilacluster.models.security.RoleStagePermission;
import org.dev.tequilacluster.models.security.RoleStagePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** FR-03/RB-504: permisos view/create/update/complete por rol+etapa. */
public interface RoleStagePermissionRepository extends JpaRepository<RoleStagePermission, RoleStagePermissionId> {

    List<RoleStagePermission> findByIdRoleId(UUID roleId);

    List<RoleStagePermission> findByIdStageCode(String stageCode);

    Optional<RoleStagePermission> findByIdRoleIdAndIdStageCode(UUID roleId, String stageCode);
}
