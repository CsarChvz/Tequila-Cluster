package org.dev.tequilacluster.config;

import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.security.Role;
import org.dev.tequilacluster.models.security.UserRole;
import org.dev.tequilacluster.models.security.UserRoleId;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.security.RoleRepository;
import org.dev.tequilacluster.repositories.security.UserRoleRepository;
import org.dev.tequilacluster.utils.security.RoleCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Creates one working demo account per role on first startup so the app is usable right after
 * {@code docker compose up} without a manual SQL insert — these are exactly the "Acceso Rápido
 * por Rol" quick-login buttons on the frontend's login page. Password for every seeded account
 * is {@code password123}. Only runs when a given username doesn't already exist; never
 * overwrites or resets an existing account.
 */
@Component
public class BootstrapDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataSeeder.class);
    private static final String BOOTSTRAP_PASSWORD = "password123";

    private record DemoAccount(String username, String email, String fullName, String roleCode) {
    }

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("admin.jcuervo", "admin@tequilacuervo.com", "Administrador José Cuervo", RoleCodes.ADMINISTRATOR),
            new DemoAccount("operador.jima", "operador.jima@tequilacuervo.com", "Operador de Jima", RoleCodes.JIMA_OPERATOR),
            new DemoAccount("operador.destilacion", "operador.destilacion@tequilacuervo.com", "Operador de Destilación", RoleCodes.DISTILLATION_OPERATOR),
            new DemoAccount("operador.envasado", "operador.envasado@tequilacuervo.com", "Operador de Envasado", RoleCodes.BOTTLING_OPERATOR),
            new DemoAccount("operador.logistica", "operador.logistica@tequilacuervo.com", "Operador de Logística", RoleCodes.LOGISTICS_OPERATOR),
            new DemoAccount("auditor.calidad", "auditor.calidad@tequilacuervo.com", "Auditor de Calidad", RoleCodes.AUDITOR)
    );

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapDataSeeder(AppUserRepository appUserRepository,
                                RoleRepository roleRepository,
                                UserRoleRepository userRoleRepository,
                                PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        DEMO_ACCOUNTS.forEach(this::seedIfMissing);
    }

    private void seedIfMissing(DemoAccount account) {
        if (appUserRepository.existsByUsername(account.username())) {
            return;
        }

        Role role = roleRepository.findByCode(account.roleCode()).orElse(null);
        if (role == null) {
            log.warn("Bootstrap account {} skipped: role {} not found — has seed_reference_data.sql run?",
                    account.username(), account.roleCode());
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(account.username());
        user.setEmail(account.email());
        user.setPasswordHash(passwordEncoder.encode(BOOTSTRAP_PASSWORD));
        user.setFullName(account.fullName());
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user = appUserRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(user.getId(), role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        log.info("Bootstrap demo user created: {} / {} (change in a real deployment)", account.username(), account.roleCode());
    }
}
