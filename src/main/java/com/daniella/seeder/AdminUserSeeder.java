package com.daniella.seeder;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.enums.UserStatus;
import com.daniella.repository.UserRepository;
import com.daniella.util.AvatarCatalog;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.default-admin.enabled:false}")
    private boolean seedDefaultAdmin;

    @Value("${app.seed.default-admin.email:}")
    private String defaultAdminEmail;

    @Value("${app.seed.default-admin.password:}")
    private String defaultAdminPassword;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedDefaultAdmin || defaultAdminEmail.isBlank() || defaultAdminPassword.isBlank()) {
            return;
        }

        List<User> users = userRepository.findAll();
        boolean adminExists = users.stream()
                .anyMatch(user -> user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_SUPERADMIN);

        if (!adminExists) {
            if (users.stream().noneMatch(user -> defaultAdminEmail.equalsIgnoreCase(user.getEmail()))) {
                User admin = new User();
                admin.setFirstName("Java");
                admin.setLastName("Jolt");
                admin.setEmail(defaultAdminEmail);
                admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setVerified(true);
                admin.setAvatarPath(AvatarCatalog.defaultAvatarFor(defaultAdminEmail));
                userRepository.save(admin);
                System.out.println("Created default admin account: " + defaultAdminEmail);
            }
        }
    }
}
