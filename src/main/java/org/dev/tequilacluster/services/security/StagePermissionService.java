package org.dev.tequilacluster.services.security;

import org.dev.tequilacluster.exceptions.ForbiddenStageActionException;
import org.dev.tequilacluster.models.security.Role;
import org.dev.tequilacluster.repositories.security.RoleRepository;
import org.dev.tequilacluster.repositories.security.RoleStagePermissionRepository;
import org.dev.tequilacluster.utils.security.StageAction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Enforces RB-504: a role acts on a stage only for the actions (view/create/update/complete)
 * its {@code role_stage_permission} row allows. Call {@link #assertAllowed} from every
 * stage-specific controller/service method before mutating data (FR-03).
 *
 * <p>The Auditor role is view-only on every stage by definition (FR-02) — this is data-driven
 * through role_stage_permission, so no special case is needed here as long as the Administrator
 * seeds that catalog correctly (see TODO in the seed/migration data, not this class).
 */
@Service
public class StagePermissionService {

    private final RoleRepository roleRepository;
    private final RoleStagePermissionRepository roleStagePermissionRepository;

    public StagePermissionService(RoleRepository roleRepository, RoleStagePermissionRepository roleStagePermissionRepository) {
        this.roleRepository = roleRepository;
        this.roleStagePermissionRepository = roleStagePermissionRepository;
    }

    public boolean isAllowed(List<String> roleCodes, String stageCode, StageAction action) {
        return roleCodes.stream().anyMatch(roleCode -> roleAllows(roleCode, stageCode, action));
    }

    public void assertAllowed(List<String> roleCodes, String stageCode, StageAction action) {
        if (!isAllowed(roleCodes, stageCode, action)) {
            throw new ForbiddenStageActionException(stageCode, action.name());
        }
    }

    private boolean roleAllows(String roleCode, String stageCode, StageAction action) {
        return roleRepository.findByCode(roleCode)
                .map(Role::getId)
                .flatMap(roleId -> roleStagePermissionRepository.findByRoleIdAndStageCode(roleId, stageCode))
                .map(permission -> switch (action) {
                    case VIEW -> Boolean.TRUE.equals(permission.getCanView());
                    case CREATE -> Boolean.TRUE.equals(permission.getCanCreate());
                    case UPDATE -> Boolean.TRUE.equals(permission.getCanUpdate());
                    case COMPLETE -> Boolean.TRUE.equals(permission.getCanComplete());
                })
                .orElse(false);
    }
}
