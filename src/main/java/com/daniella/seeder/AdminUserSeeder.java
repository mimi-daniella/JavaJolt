package com.daniella.seeder;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.daniella.entity.User;
import com.daniella.enums.Role;
import com.daniella.enums.UserStatus;
import com.daniella.repository.UserRepository;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        boolean adminExists = users.stream()
                .anyMatch(user -> user.getRole() == Role.ROLE_ADMIN || user.getRole() == Role.ROLE_SUPERADMIN);

        if (!adminExists) {
            if (users.stream().noneMatch(user -> "admin@javajolt.com".equalsIgnoreCase(user.getEmail()))) {
                User admin = new User();
                admin.setFirstName("Java");
                admin.setLastName("Jolt");
                admin.setEmail("admin@javajolt.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setVerified(true);
                userRepository.save(admin);
                System.out.println("Created default admin account: admin@javajolt.com / Admin@123");
            }
        }
    }
}
