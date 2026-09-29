package org.dev.tequilacluster.repositories.security;

import org.dev.tequilacluster.models.security.UserRole;
import org.dev.tequilacluster.models.security.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-02: soporta multi-rol por usuario (user_id, role_id). */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
}
