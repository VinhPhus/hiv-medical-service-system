package com.hivcare.controller;

import com.hivcare.entity.TestResult;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.service.TestResultService;
import com.hivcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/testResult")
public class TestResultWebController {
    private static final Logger logger = LoggerFactory.getLogger(TestResultWebController.class);

    @Autowired
    private TestResultService testResultService;

    @Autowired
    private PatientService patientService;

    private boolean isStaff(User.Role role) {
        return role == User.Role.ADMIN || role == User.Role.DOCTOR || 
               role == User.Role.MANAGER || role == User.Role.STAFF;
    }

    @GetMapping("/list")
    public String listTestResults(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        List<TestResult> testResults = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                user = (User) principal;
                logger.info("Listing test results for user: {} with role: {}", user.getUsername(), user.getRole());
                if (user.getRole() == User.Role.PATIENT) {
                    Patient patient = patientService.getPatientByUserId(user.getId())
                            .orElse(null);
                    if (patient != null) {
                        testResults = testResultService.getTestResultsByPatient(patient.getId());
                        model.addAttribute("patientId", patient.getId());
                        logger.info("Patient ID set for user: {}", patient.getId());
                    } else {
                        testResults = java.util.Collections.emptyList();
                    }
                } else {
                    testResults = testResultService.getAllTestResults();
                }
            }
        } else {
            logger.info("Listing test results for anonymous user");
            testResults = java.util.Collections.emptyList();
        }
        model.addAttribute("testResults", testResults);
        model.addAttribute("testTypes", TestResult.TestType.values());
        model.addAttribute("testStatuses", TestResult.TestStatus.values());
        // Thống kê
        long total = testResults.size();
        long completed = testResults.stream().filter(r -> r.getStatus() == TestResult.TestStatus.COMPLETED).count();
        long pending = testResults.stream().filter(r -> r.getStatus() == TestResult.TestStatus.PENDING).count();
        long abnormal = testResults.stream().filter(r -> r.getStatus() == TestResult.TestStatus.ABNORMAL).count();
        model.addAttribute("totalTests", total);
        model.addAttribute("completedTests", completed);
        model.addAttribute("pendingTests", pending);
        model.addAttribute("abnormalTests", abnormal);
        return "testResult/list";
    }

    @GetMapping("/form")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'MANAGER', 'STAFF')")
    public String showTestResultForm(@RequestParam(required = false) Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (!(principal instanceof User user)) {
            return "redirect:/auth/login";
        }
        logger.info("Showing test result form for user: {} with role: {}", user.getUsername(), user.getRole());
        // Chỉ cho phép nhân viên truy cập form
        if (!isStaff(user.getRole())) {
            logger.warn("Access denied: User {} with role {} attempting to access test result form", 
                user.getUsername(), user.getRole());
            return "error/403";
        }
        TestResult testResult = id != null 
            ? testResultService.getTestResultById(id).orElse(new TestResult())
            : new TestResult();
        // Thêm danh sách bệnh nhân cho dropdown
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("testResult", testResult);
        model.addAttribute("testTypes", TestResult.TestType.values());
        model.addAttribute("testStatuses", TestResult.TestStatus.values());
        return "testResult/form";
    }

    @GetMapping("/detail/{id}")
    public String showTestResultDetail(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (!(principal instanceof User user)) {
            return "redirect:/auth/login";
        }
        logger.info("Showing test result detail for ID: {} by user: {} with role: {}", 
            id, user.getUsername(), user.getRole());
        TestResult testResult = testResultService.getTestResultById(id)
                .orElseThrow(() -> new RuntimeException("Test result not found"));
        // Kiểm tra quyền truy cập
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientService.getPatientByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            if (!testResult.getPatient().getId().equals(patient.getId())) {
                logger.warn("Access denied: Patient {} attempting to access test result {}", 
                    patient.getId(), id);
                return "error/403";
            }
        }
        model.addAttribute("testResult", testResult);
        return "testResult/detail";
    }

    @PostMapping("/save")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'MANAGER', 'STAFF')")
    public String saveTestResult(@ModelAttribute TestResult testResult) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (!(principal instanceof User user)) {
            return "redirect:/auth/login";
        }
        logger.info("Saving test result by user: {} with role: {}", user.getUsername(), user.getRole());
        // Chỉ cho phép nhân viên lưu kết quả xét nghiệm
        if (!isStaff(user.getRole())) {
            logger.warn("Access denied: User {} with role {} attempting to save test result", 
                user.getUsername(), user.getRole());
            return "error/403";
        }
        if (testResult.getId() == null) {
            testResultService.createTestResult(testResult);
            logger.info("Created new test result");
        } else {
            testResultService.updateTestResult(testResult.getId(), testResult);
            logger.info("Updated test result with ID: {}", testResult.getId());
        }
        return "redirect:/testResult/list";
    }

    @GetMapping("/download/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'MANAGER', 'STAFF', 'PATIENT')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(302).header("Location", "/auth/login").build();
        }
        User user = (User) auth.getPrincipal();
        logger.info("Downloading file for test result ID: {} by user: {} with role: {}", 
            id, user.getUsername(), user.getRole());

        TestResult testResult = testResultService.getTestResultById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả xét nghiệm với ID: " + id));

        // Kiểm tra quyền truy cập
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientService.getPatientByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            if (!testResult.getPatient().getId().equals(patient.getId())) {
                logger.warn("Access denied: Patient {} attempting to download file for test result {}", 
                    patient.getId(), id);
                throw new RuntimeException("Access denied");
            }
        }

        if (testResult.getFilePath() == null) {
            throw new RuntimeException("Không có file đính kèm");
        }

        byte[] fileContent = testResultService.getFile(testResult.getFilePath());
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + testResult.getFilePath())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileContent.length)
                .body(resource);
    }

    @GetMapping("/api/test-results")
    @ResponseBody
    public Page<TestResult> getTestResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "testDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        User user = (User) auth.getPrincipal();
        logger.info("Getting test results page {} for user: {} with role: {}", 
            page, user.getUsername(), user.getRole());

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Nếu là bệnh nhân, chỉ trả về kết quả của họ
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientService.getPatientByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            return testResultService.getTestResultsByPatientId(patient.getId(), search, pageable);
        }

        // Nhân viên có thể xem tất cả kết quả
        return testResultService.getAllTestResults(search, pageable);
    }

    @GetMapping("/api/test-results/stats")
    @ResponseBody
    public Map<String, Long> getTestResultStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        User user = (User) auth.getPrincipal();
        logger.info("Getting test result stats for user: {} with role: {}", 
            user.getUsername(), user.getRole());

        // Chỉ cho phép nhân viên xem thống kê
        if (!isStaff(user.getRole())) {
            logger.warn("Access denied: User {} with role {} attempting to view test result stats", 
                user.getUsername(), user.getRole());
            throw new RuntimeException("Access denied");
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", testResultService.countTotalTests());
        stats.put("completed", testResultService.countTestsByStatus(TestResult.TestStatus.COMPLETED));
        stats.put("pending", testResultService.countTestsByStatus(TestResult.TestStatus.PENDING));
        stats.put("abnormal", testResultService.countTestsByStatus(TestResult.TestStatus.ABNORMAL));
        return stats;
    }
}
