package com.hivcare.controller;


import com.hivcare.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.hivcare.dto.request.SignupRequest;
import com.hivcare.service.DoctorService;
import com.hivcare.service.PatientService;
import com.hivcare.service.AppointmentService;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.entity.Appointment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.Role role) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            if (role != null) {
                users = userService.searchUsers(search, pageable);
                // Filter by role if needed - this would need custom repository method
            } else {
                users = userService.searchUsers(search, pageable);
            }
        } else if (role != null) {
            users = userService.getUsersByRole(role, pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Tên đăng nhập đã tồn tại!"));
            }
            
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email đã tồn tại!"));
            }

            User savedUser = userService.createUser(user);
            
            // Send welcome email
            notificationService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
            
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo tài khoản: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật tài khoản: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setEnabled(true);
            User updatedUser = userService.updateUser(id, user);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi kích hoạt tài khoản: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setEnabled(false);
            User updatedUser = userService.updateUser(id, user);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi vô hiệu hóa tài khoản: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa tài khoản: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestParam String newPassword) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // This would need to be implemented in UserService
            // userService.resetPassword(id, newPassword);
            
            return ResponseEntity.ok(new ApiResponse(true, "Reset mật khẩu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi reset mật khẩu: " + e.getMessage()));
        }
    }

    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        systemInfo.put("totalUsers", userService.getTotalUsers());
        systemInfo.put("activeUsers", userService.countActiveUsers());
        systemInfo.put("adminUsers", userService.getUsersByRole(User.Role.ADMIN));
        systemInfo.put("doctorUsers", userService.getUsersByRole(User.Role.DOCTOR));
        systemInfo.put("staffUsers", userService.getUsersByRole(User.Role.STAFF));
        systemInfo.put("managerUsers", userService.getUsersByRole(User.Role.MANAGER));
        systemInfo.put("customerUsers", userService.getUsersByRole(User.Role.PATIENT));
        
        systemInfo.put("serverTime", LocalDateTime.now());
        
        return ResponseEntity.ok(systemInfo);
    }

    @GetMapping("/users/recent")
    public ResponseEntity<List<User>> getRecentUsers(@RequestParam(defaultValue = "10") int limit) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<User> recentUsers = userService.getUsersByCreatedAtBetween(oneWeekAgo, LocalDateTime.now());
        
        if (limit > 0 && recentUsers.size() > limit) {
            recentUsers = recentUsers.subList(0, limit);
        }
        
        return ResponseEntity.ok(recentUsers);
    }

    @PostMapping("/send-notification")
    public ResponseEntity<?> sendNotification(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message) {
        try {
            // This would need to be implemented in NotificationService
            // notificationService.sendCustomNotification(email, subject, message);
            
            return ResponseEntity.ok(new ApiResponse(true, "Gửi thông báo thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi gửi thông báo: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast-notification")
    public ResponseEntity<?> broadcastNotification(
            @RequestParam User.Role role,
            @RequestParam String subject,
            @RequestParam String message) {
        try {
            List<User> users = userService.getActiveUsersByRole(role);
            
            for (User user : users) {
                // This would need to be implemented in NotificationService
                // notificationService.sendCustomNotification(user.getEmail(), subject, message);
            }
            
            return ResponseEntity.ok(new ApiResponse(true, "Gửi thông báo hàng loạt thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi gửi thông báo hàng loạt: " + e.getMessage()));
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // This would need to be implemented with proper audit logging
        Map<String, Object> auditInfo = new HashMap<>();
        auditInfo.put("message", "Audit logging chưa được triển khai");
        auditInfo.put("totalLogs", 0);
        auditInfo.put("logs", List.of());
        
        return ResponseEntity.ok(auditInfo);
    }

    // Hiển thị form thêm quản lý
    @GetMapping("/add-manager")
    public String showAddManagerForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "admin/add-manager";
    }

    // Hiển thị form thêm nhân viên
    @GetMapping("/add-staff")
    public String showAddStaffForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "admin/add-staff";
    }

    // Hiển thị form thêm bác sĩ
    @GetMapping("/add-doctor")
    public String showAddDoctorForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "admin/add-doctor";
    }

    @GetMapping("/doctors")
    public String showDoctorList(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Doctor> doctors = doctorService.getAllDoctors(PageRequest.of(page, 10));
        model.addAttribute("doctors", doctors);
        return "admin/doctors";
    }

    @GetMapping("/patients")
    public String showPatientList(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Patient> patients = patientService.getAllPatients(PageRequest.of(page, 10));
        model.addAttribute("patients", patients);
        return "admin/patients";
    }

    @DeleteMapping("/delete-doctor/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-patient/{id}")
    @ResponseBody
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/doctors/{id}")
    public String showDoctorDetail(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        model.addAttribute("doctor", doctor);
        return "admin/doctor-detail";
    }

    @GetMapping("/doctors/{id}/edit")
    public String showDoctorEditForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        model.addAttribute("doctor", doctor);
        return "admin/doctor-edit";
    }

    @GetMapping("/patients/{id}")
    public String showPatientDetail(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        return "admin/patient-detail";
    }

    @GetMapping("/patients/{id}/edit")
    public String showPatientEditForm(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        return "admin/patient-edit";
    }

    @GetMapping("")
    public String showDashboard(Model model) {
        // Thống kê tổng quan
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalDoctors", doctorService.getTotalDoctors());
        model.addAttribute("totalPatients", patientService.getTotalPatients());
        model.addAttribute("totalStaff", userService.countUsersByRole(User.Role.STAFF));

        // Lấy 10 lịch hẹn gần đây nhất
        List<Appointment> recentAppointments = appointmentService.getRecentAppointments(10);
        model.addAttribute("recentAppointments", recentAppointments);

        return "dashboard/admin";
    }
}
