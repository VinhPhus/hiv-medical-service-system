package com.hivcare.config;
import com.hivcare.entity.User;
import com.hivcare.enums.UserRole;
import com.hivcare.enums.UserStatus;
import com.hivcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing default users...");

            // Create Patient User
            createUser("patient@hiv.care", "patient123", "Nguyễn Văn A", UserRole.CUSTOMER);

            // Create Doctor User
            createUser("doctor@hiv.care", "doctor123", "BS. Trần Thị B", UserRole.DOCTOR);

            // Create Admin User
            createUser("admin@hiv.care", "admin123", "Quản trị viên", UserRole.ADMIN);

            // Create Manager User
            createUser("manager@hiv.care", "manager123", "Lê Văn C", UserRole.MANAGER);

            log.info("Default users created successfully");
        }
    }

    private void createUser(String email, String password, String fullName, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        userRepository.save(user);
        log.info("Created user: {} with role: {}", email, role);
    }
}
