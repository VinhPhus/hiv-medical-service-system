package com.hivcare.controller;

import com.hivcare.dto.request.TestResultRequest;
import com.hivcare.dto.response.TestResultResponse;
import com.hivcare.enums.TestType;
import com.hivcare.service.TestResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/test-results")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class TestResultController {

    private final TestResultService testResultService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TestResultResponse>> getTestResults(Authentication authentication) {
        List<TestResultResponse> results = testResultService.getPatientTestResults(authentication);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/type/{testType}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TestResultResponse>> getTestResultsByType(
            @PathVariable TestType testType,
            Authentication authentication) {
        List<TestResultResponse> results = testResultService.getTestResultsByType(testType, authentication);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TestResultResponse> createTestResult(
            @Valid @RequestBody TestResultRequest request,
            Authentication authentication) {
        TestResultResponse response = testResultService.createTestResult(request, authentication);
        return ResponseEntity.ok(response);
    }
}
