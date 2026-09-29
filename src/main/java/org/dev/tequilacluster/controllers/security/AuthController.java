package org.dev.tequilacluster.controllers.security;

import jakarta.validation.Valid;
import org.dev.tequilacluster.dtos.security.LoginRequest;
import org.dev.tequilacluster.dtos.security.LoginResponse;
import org.dev.tequilacluster.dtos.security.RegisterRequest;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.security.Role;
import org.dev.tequilacluster.models.security.UserRole;
import org.dev.tequilacluster.models.security.UserRoleId;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.security.RoleRepository;
import org.dev.tequilacluster.repositories.security.UserRoleRepository;
import org.dev.tequilacluster.services.security.JwtService;
import org.dev.tequilacluster.utils.security.AppUserPrincipal;
import org.dev.tequilacluster.utils.security.RoleCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** FR-01: authenticate users and issue the session JWT. FR-02: self-service operator registration. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    /** Self-registration can only grant operator/auditor roles — Administrator is assigned by an existing Administrator (FR-04), never self-selected. */
    private static final Set<String> SELF_REGISTERABLE_ROLES = Set.of(
            RoleCodes.JIMA_OPERATOR,
            RoleCodes.DISTILLATION_OPERATOR,
            RoleCodes.BOTTLING_OPERATOR,
            RoleCodes.LOGISTICS_OPERATOR,
            RoleCodes.AUDITOR
    );

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           AppUserRepository appUserRepository,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getUserId(), principal.getUsername(), principal.getAuthorities());

        return ResponseEntity.ok(new LoginResponse(token, principal.getUserId(), principal.getUsername(), principal.getRoleCodes()));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new BusinessRuleViolationException("FR-01", "Username already exists: " + request.username());
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BusinessRuleViolationException("FR-01", "Email already registered: " + request.email());
        }
        if (!SELF_REGISTERABLE_ROLES.contains(request.roleCode())) {
            throw new BusinessRuleViolationException("FR-04",
                    "Role cannot be self-assigned at registration: " + request.roleCode() + " (an Administrator must grant it)");
        }
        Role role = roleRepository.findByCode(request.roleCode())
                .orElseThrow(() -> new BusinessRuleViolationException("FR-02", "Unknown role: " + request.roleCode()));

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.username());
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = appUserRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(user.getId(), role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
        String token = jwtService.generateToken(user.getId(), user.getUsername(), authorities);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(token, user.getId(), user.getUsername(), List.of(role.getCode())));
    }
}
