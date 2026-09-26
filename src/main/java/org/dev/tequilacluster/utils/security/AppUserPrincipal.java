package org.dev.tequilacluster.utils.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Spring Security principal wrapping an {@code app_user} row plus its role codes (FR-01, FR-03). */
public class AppUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final List<String> roleCodes;

    public AppUserPrincipal(UUID userId, String username, String passwordHash, boolean active, List<String> roleCodes) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roleCodes = roleCodes;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream().map(code -> new SimpleGrantedAuthority("ROLE_" + code)).toList();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
