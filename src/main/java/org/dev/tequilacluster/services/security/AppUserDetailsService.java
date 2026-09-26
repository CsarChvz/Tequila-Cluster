package org.dev.tequilacluster.services.security;

import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.security.Role;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.security.RoleRepository;
import org.dev.tequilacluster.repositories.security.UserRoleRepository;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Loads a user and its role codes for authentication (FR-01) and JWT claim population. */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository, UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));
        List<String> roleCodes = roleCodesOf(user.getId());
        return new AppUserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), Boolean.TRUE.equals(user.getActive()), roleCodes);
    }

    private List<String> roleCodesOf(UUID userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(userRole -> roleRepository.findById(userRole.getId().getRoleId())
                        .map(Role::getCode)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
