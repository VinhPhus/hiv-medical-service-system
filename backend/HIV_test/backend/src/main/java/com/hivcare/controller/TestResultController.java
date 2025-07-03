package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.TestResult;
import com.hivcare.entity.Patient;
import com.hivcare.service.TestResultService;
import com.hivcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/test-results")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestResultController {
    private static final Logger logger = LoggerFactory.getLogger(TestResultController.class);

    @Autowired
    private TestResultService testResultService;

    @Autowired
    private PatientService patientService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<?> getAllTestResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "testDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TestResult> testResults = testResultService.getAllTestResults(search, pageable);
            
            return ResponseEntity.ok(testResults);
        } catch (Exception e) {
            logger.error("Error getting test results: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy danh sách kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<TestResult> getTestResultById(@PathVariable Long id) {
        return testResultService.getTestResultById(id)
                .map(testResult -> ResponseEntity.ok().body(testResult))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<TestResult> getTestResultByCode(@PathVariable String code) {
        return testResultService.getTestResultByCode(code)
                .map(testResult -> ResponseEntity.ok().body(testResult))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<Page<TestResult>> getTestResultsByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("testDate").descending());
        Page<TestResult> testResults = testResultService.getTestResultsByPatient(patientId, pageable);
        
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/my-results")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<TestResult>> getMyTestResults() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Patient patient = patientService.getPatientByUserId(getUserIdFromUsername(username))
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        List<TestResult> testResults = testResultService.getTestResultsByPatient(patient.getId());
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/my-results/{testType}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<TestResult>> getMyTestResultsByType(@PathVariable TestResult.TestType testType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Patient patient = patientService.getPatientByUserId(getUserIdFromUsername(username))
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        List<TestResult> testResults = testResultService.getTestResultsByPatientAndType(patient.getId(), testType);
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/type/{testType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<List<TestResult>> getTestResultsByType(@PathVariable TestResult.TestType testType) {
        List<TestResult> testResults = testResultService.getTestResultsByType(testType);
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<Integer> getTestResultsByStatus(@PathVariable TestResult.TestStatus status) {
        int testResults = testResultService.getTestResultsByStatus(status);
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<List<TestResult>> getPendingTestResults() {
        List<TestResult> testResults = testResultService.getPendingTestResults();
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/abnormal")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<List<TestResult>> getAbnormalTestResults() {
        List<TestResult> testResults = testResultService.getAbnormalTestResults();
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<List<TestResult>> getTestResultsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<TestResult> testResults = testResultService.getTestResultsByDateRange(startDate, endDate);
        return ResponseEntity.ok(testResults);
    }

    @GetMapping("/patient/{patientId}/cd4-history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<List<TestResult>> getCd4History(@PathVariable Long patientId) {
        List<TestResult> cd4Results = testResultService.getCd4History(patientId);
        return ResponseEntity.ok(cd4Results);
    }

    @GetMapping("/patient/{patientId}/viral-load-history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<List<TestResult>> getViralLoadHistory(@PathVariable Long patientId) {
        List<TestResult> viralLoadResults = testResultService.getViralLoadHistory(patientId);
        return ResponseEntity.ok(viralLoadResults);
    }

    @GetMapping("/patient/{patientId}/latest/{testType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<TestResult> getLatestTestResult(@PathVariable Long patientId, @PathVariable TestResult.TestType testType) {
        TestResult latestResult = testResultService.getLatestTestResult(patientId, testType);
        if (latestResult != null) {
            return ResponseEntity.ok(latestResult);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> createTestResult(@RequestBody TestResult testResult) {
        try {
            TestResult savedTestResult = testResultService.createTestResult(testResult);
            return ResponseEntity.ok(savedTestResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> updateTestResult(@PathVariable Long id, @RequestBody TestResult testResultDetails) {
        try {
            TestResult updatedTestResult = testResultService.updateTestResult(id, testResultDetails);
            return ResponseEntity.ok(updatedTestResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> completeTestResult(
            @PathVariable Long id, 
            @RequestParam String resultValue, 
            @RequestParam(required = false) String notes) {
        try {
            TestResult completedTestResult = testResultService.completeTestResult(id, resultValue, notes);
            return ResponseEntity.ok(completedTestResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi hoàn thành kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> reviewTestResult(@PathVariable Long id, @RequestParam String reviewNotes) {
        try {
            TestResult reviewedTestResult = testResultService.reviewTestResult(id, reviewNotes);
            return ResponseEntity.ok(reviewedTestResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi đánh giá kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTestResult(@PathVariable Long id) {
        try {
            testResultService.deleteTestResult(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa kết quả xét nghiệm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa kết quả xét nghiệm: " + e.getMessage()));
        }
    }

    // Helper method to get user ID from username
    private Long getUserIdFromUsername(String username) {
        try {
            // This should be implemented properly based on your UserService
            return 1L; // Placeholder
        } catch (Exception e) {
            logger.error("Error getting user ID from username: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi lấy thông tin người dùng", e);
        }
    }
}
