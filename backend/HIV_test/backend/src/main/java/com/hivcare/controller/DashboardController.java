package com.hivcare.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivcare.entity.Appointment;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.MedicalRecord;
import com.hivcare.entity.Patient;
import com.hivcare.entity.TestResult;
import com.hivcare.entity.Treatment;
import com.hivcare.entity.User;
import com.hivcare.service.ApiService;
import com.hivcare.service.AppointmentService;
import com.hivcare.service.DoctorService;
import com.hivcare.service.MedicalRecordService;
import com.hivcare.service.PatientService;
import com.hivcare.service.TestResultService;
import com.hivcare.service.TreatmentService;
import com.hivcare.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ApiService apiService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private TestResultService testResultService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User getCurrentUser(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Current authentication: principal={}, authorities={}", 
            auth.getPrincipal(), auth.getAuthorities());
        
        User user = null;
        if (session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        } else if (auth.getPrincipal() instanceof User) {
            user = (User) auth.getPrincipal();
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole().name());
        }
        
        if (user == null) {
            logger.error("User không tồn tại trong session và authentication");
            throw new RuntimeException("Phiên làm việc không hợp lệ");
        }

        return user;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminDashboard(Model model, HttpSession session) {
        try {
            logger.info("Bắt đầu xử lý admin dashboard");
            
            User user = getCurrentUser(session);
            logger.info("User info: {}", user);
            
            // Thêm thông tin người dùng
            model.addAttribute("user", user);
            model.addAttribute("adminName", user.getFullName());

            try {
                // Thêm số liệu thống kê
                long totalUsers = userService.getTotalUsers();
                long totalDoctors = doctorService.getTotalDoctors();
                long totalPatients = patientService.getTotalPatients();
                List<Appointment> todayAppts = appointmentService.getTodayAppointments();

                logger.info("Thống kê: users={}, doctors={}, patients={}, appointments={}",
                    totalUsers, totalDoctors, totalPatients, todayAppts != null ? todayAppts.size() : 0);

                model.addAttribute("totalUsers", totalUsers);
                model.addAttribute("totalDoctors", totalDoctors);
                model.addAttribute("totalPatients", totalPatients);
                model.addAttribute("todayAppointments", todayAppts != null ? todayAppts : Collections.emptyList());
                model.addAttribute("recentActivities", Collections.emptyList());
            } catch (Exception e) {
                logger.error("Lỗi khi lấy thống kê: {}", e.getMessage());
                model.addAttribute("totalUsers", 0);
                model.addAttribute("totalDoctors", 0);
                model.addAttribute("totalPatients", 0);
                model.addAttribute("todayAppointments", Collections.emptyList());
                model.addAttribute("recentActivities", Collections.emptyList());
            }

            return "dashboard/admin";
        } catch (Exception e) {
            logger.error("Lỗi không xác định trong admin dashboard: {}", e.getMessage());
            logger.error("Chi tiết lỗi:", e);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            return "error/500";
        }
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public String doctorDashboard(
            Model model, 
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Bắt đầu xử lý doctor dashboard");
            
            User user = getCurrentUser(session);
            logger.info("User info: {}", user);
            
            // Thêm thông tin người dùng
            model.addAttribute("user", user);
            model.addAttribute("doctorName", user.getFullName());

            try {
                // Lấy thông tin bác sĩ
                Doctor doctor = doctorService.getDoctorByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));

                // Phân trang danh sách bệnh nhân
                Pageable pageable = PageRequest.of(page, size);
                Page<Patient> doctorPatients = patientService.getPatientsByDoctorPaged(doctor.getId(), pageable);
                
                List<Appointment> todayAppts = appointmentService.getTodayAppointmentsByDoctor(doctor.getId());
                long totalPatients = patientService.countPatientsByDoctor(doctor.getId());
                long pendingAppointments = appointmentService.countPendingAppointmentsByDoctor(doctor.getId());
                long totalMedicalRecords = medicalRecordService.countMedicalRecordsByDoctor(doctor.getId());
                List<TestResult> pendingTests = testResultService.getPendingTestsByDoctor(doctor.getId());

                logger.info("Thống kê bác sĩ: patients={}, appointments={}, pendingAppointments={}, medicalRecords={}, pendingTests={}",
                    totalPatients, todayAppts != null ? todayAppts.size() : 0, pendingAppointments, totalMedicalRecords,
                    pendingTests != null ? pendingTests.size() : 0);

                model.addAttribute("totalPatients", totalPatients);
                model.addAttribute("todayAppointments", todayAppts != null ? todayAppts : Collections.emptyList());
                model.addAttribute("pendingAppointments", pendingAppointments);
                model.addAttribute("totalMedicalRecords", totalMedicalRecords);
                model.addAttribute("pendingTests", pendingTests != null ? pendingTests : Collections.emptyList());
                model.addAttribute("patients", doctorPatients);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", doctorPatients.getTotalPages());
            } catch (Exception e) {
                logger.error("Lỗi khi lấy thống kê bác sĩ: {}", e.getMessage());
                model.addAttribute("error", "Không thể lấy thông tin. " + e.getMessage());
                model.addAttribute("totalPatients", 0);
                model.addAttribute("todayAppointments", Collections.emptyList());
                model.addAttribute("pendingAppointments", 0);
                model.addAttribute("totalMedicalRecords", 0);
                model.addAttribute("pendingTests", Collections.emptyList());
                model.addAttribute("patients", Page.empty());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
            }

            return "dashboard/doctor";
        } catch (Exception e) {
            logger.error("Lỗi không xác định trong doctor dashboard: {}", e.getMessage());
            logger.error("Chi tiết lỗi:", e);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            return "error/500";
        }
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public String managerDashboard(Model model, HttpSession session) {
        try {
            logger.info("Bắt đầu xử lý manager dashboard");
            
            // Kiểm tra xác thực
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.error("Người dùng chưa được xác thực");
                return "redirect:/auth/login";
            }
            
            // Lấy thông tin người dùng
            User user = null;
            try {
                user = getCurrentUser(session);
                if (user == null) {
                    logger.error("Không thể lấy thông tin người dùng");
                    return "redirect:/auth/login";
                }
            } catch (Exception e) {
                logger.error("Lỗi khi lấy thông tin người dùng: {}", e.getMessage());
                return "redirect:/auth/login";
            }
            
            // Thêm thông tin người dùng vào model
            model.addAttribute("user", user);

            // Thống kê bác sĩ
            try {
                long totalDoctors = doctorService.getTotalDoctors();
                model.addAttribute("totalDoctors", totalDoctors);
                logger.info("Tổng số bác sĩ: {}", totalDoctors);
            } catch (Exception e) {
                logger.error("Lỗi khi lấy tổng số bác sĩ: {}", e.getMessage());
                model.addAttribute("totalDoctors", 0L);
            }

            // Thống kê bệnh nhân
            try {
                long totalPatients = patientService.getTotalPatients();
                model.addAttribute("totalPatients", totalPatients);
                logger.info("Tổng số bệnh nhân: {}", totalPatients);
            } catch (Exception e) {
                logger.error("Lỗi khi lấy tổng số bệnh nhân: {}", e.getMessage());
                model.addAttribute("totalPatients", 0L);
            }

            // Thống kê lịch hẹn hôm nay
            try {
                List<Appointment> todayAppts = appointmentService.getTodayAppointments();
                model.addAttribute("todayAppointments", todayAppts != null ? todayAppts : Collections.emptyList());
                logger.info("Số lịch hẹn hôm nay: {}", todayAppts != null ? todayAppts.size() : 0);
            } catch (Exception e) {
                logger.error("Lỗi khi lấy danh sách lịch hẹn hôm nay: {}", e.getMessage());
                model.addAttribute("todayAppointments", Collections.emptyList());
            }

            // Thống kê xét nghiệm tháng này
            try {
                List<TestResult> monthlyTests = testResultService.getMonthlyTests();
                model.addAttribute("monthlyTests", monthlyTests != null ? monthlyTests : Collections.emptyList());
                logger.info("Số xét nghiệm tháng này: {}", monthlyTests != null ? monthlyTests.size() : 0);
            } catch (Exception e) {
                logger.error("Lỗi khi lấy danh sách xét nghiệm tháng này: {}", e.getMessage());
                model.addAttribute("monthlyTests", Collections.emptyList());
            }

            return "dashboard/manager";
        } catch (Exception e) {
            logger.error("Lỗi không xác định trong manager dashboard: {}", e.getMessage());
            logger.error("Chi tiết lỗi:", e);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            return "error/500";
        }
    }


    @GetMapping("/patient")
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    public String patientDashboard(Model model, HttpSession session) {
        try {
            logger.info("Bắt đầu xử lý patient dashboard");
            
            User user = getCurrentUser(session);
            logger.info("User info: {}", user);
            
            // Thêm thông tin người dùng
            model.addAttribute("user", user);
            model.addAttribute("patientName", user.getFullName());

            try {
                // Lấy thông tin bệnh nhân
                Patient patient = patientService.getPatientByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bệnh nhân"));

                // Add patient specific data
                List<Appointment> upcomingAppts = appointmentService.getUpcomingAppointmentsByPatient(patient.getId());
                List<TestResult> recentTests = testResultService.getRecentTestResultsByPatient(patient.getId(), 5);
                Optional<MedicalRecord> latestRecord = medicalRecordService.getLatestMedicalRecordByPatient(patient.getId());
                Optional<Treatment> currentTreatment = treatmentService.getCurrentTreatmentByPatient(patient.getId());

                logger.info("Thống kê bệnh nhân: appointments={}, tests={}, hasRecord={}, hasTreatment={}",
                    upcomingAppts != null ? upcomingAppts.size() : 0, 
                    recentTests != null ? recentTests.size() : 0,
                    latestRecord.isPresent(),
                    currentTreatment.isPresent());

                model.addAttribute("patient", patient);
                model.addAttribute("upcomingAppointments", upcomingAppts != null ? upcomingAppts : Collections.emptyList());
                model.addAttribute("recentTestResults", recentTests != null ? recentTests : Collections.emptyList());
                if (latestRecord.isPresent()) {
                    model.addAttribute("latestMedicalRecord", latestRecord.get());
                } else {
                    logger.debug("Không tìm thấy hồ sơ y tế nào cho bệnh nhân {}", patient.getId());
                }
                currentTreatment.ifPresent(treatment -> model.addAttribute("currentTreatment", treatment));
            } catch (Exception e) {
                logger.error("Lỗi khi lấy thông tin bệnh nhân: {}", e.getMessage());
                model.addAttribute("error", "Không thể lấy thông tin. " + e.getMessage());
                model.addAttribute("upcomingAppointments", Collections.emptyList());
                model.addAttribute("recentTestResults", Collections.emptyList());
            }

            return "dashboard/patient";
        } catch (Exception e) {
            logger.error("Lỗi không xác định trong patient dashboard: {}", e.getMessage());
            logger.error("Chi tiết lỗi:", e);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            return "error/500";
        }
    }
}

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
class DashboardRestController {
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private TestResultService testResultService;

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorDashboard() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            Doctor doctor = doctorService.getDoctorByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));

            // Lấy thông tin thống kê
            long totalPatients = patientService.countPatientsByDoctor(doctor.getId());
            long totalMedicalRecords = medicalRecordService.countMedicalRecordsByDoctor(doctor.getId());
            List<TestResult> pendingTests = testResultService.getPendingTestsByDoctor(doctor.getId());
            List<Appointment> todayAppointments = appointmentService.getTodayAppointmentsByDoctor(doctor.getId());

            HashMap<String, Object> result = new HashMap<>();
            result.put("totalPatients", totalPatients);
            result.put("totalMedicalRecords", totalMedicalRecords);
            result.put("pendingTests", pendingTests != null ? pendingTests : Collections.emptyList());
            result.put("todayAppointments", todayAppointments != null ? todayAppointments : Collections.emptyList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
}
