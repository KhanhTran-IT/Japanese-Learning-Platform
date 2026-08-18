package com.japaneselearning.common.config;

import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        boolean isSeeded = false;
        for (RoleName roleName : RoleName.values()) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description("Default role " + roleName.name())
                        .build();
                roleRepository.save(role);
                isSeeded = true;
            }
        }
        if (isSeeded) {
            log.info("DatabaseSeeder: Seeded missing roles.");
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail(adminEmail)) {
            log.warn("WARNING: Creating default admin user with default password. Please change it in production!");
            
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found. Cannot seed admin user."));

            User adminUser = User.builder()
                    .email(adminEmail)
                    .fullName("Administrator")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            userRepository.save(adminUser);
            log.info("DatabaseSeeder: Seeded default admin user: {}", adminEmail);
        }
    }
}