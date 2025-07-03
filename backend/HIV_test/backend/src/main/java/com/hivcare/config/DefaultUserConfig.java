package com.hivcare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hivcare.entity.User;
import com.hivcare.repository.UserRepository;

@Component
public class DefaultUserConfig implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra nếu chưa có user admin thì tạo mới
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@hivcare.com");
            admin.setFullName("Quản trị viên hệ thống");
            admin.setPhoneNumber("0123456789");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            admin.setAnonymous(false);
            userRepository.save(admin);
            System.out.println("Created default admin user");
        }

        // Tạo user doctor nếu chưa có
        if (!userRepository.existsByUsername("doctor1")) {
            User doctor = new User();
            doctor.setUsername("doctor1");
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setEmail("doctor1@hivcare.com");
            doctor.setFullName("BS. Nguyễn Văn An");
            doctor.setPhoneNumber("0987654321");
            doctor.setRole(User.Role.DOCTOR);
            doctor.setEnabled(true);
            doctor.setAnonymous(false);
            userRepository.save(doctor);
            System.out.println("Created default doctor user");
        }

        // Tạo user staff nếu chưa có
        if (!userRepository.existsByUsername("staff1")) {
            User staff = new User();
            staff.setUsername("staff1");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setEmail("staff1@hivcare.com");
            staff.setFullName("Nhân viên Y tế");
            staff.setPhoneNumber("0912345678");
            staff.setRole(User.Role.STAFF);
            staff.setEnabled(true);
            staff.setAnonymous(false);
            userRepository.save(staff);
            System.out.println("Created default staff user");
        }

        // Tạo user manager nếu chưa có
        if (!userRepository.existsByUsername("manager1")) {
            User manager = new User();
            manager.setUsername("manager1");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@hivcare.com");
            manager.setFullName("Quản lý Y tế");
            manager.setPhoneNumber("0898765432");
            manager.setRole(User.Role.MANAGER);
            manager.setEnabled(true);
            manager.setAnonymous(false);
            userRepository.save(manager);
            System.out.println("Created default manager user");
        }
    }
} 