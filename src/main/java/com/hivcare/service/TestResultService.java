package com.hivcare.service;

import com.hivcare.dto.request.TestResultRequest;
import com.hivcare.dto.response.TestResultResponse;
import com.hivcare.entity.User;
import com.hivcare.enums.TestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestResultService {

    @SuppressWarnings("unused")
    public List<TestResultResponse> getPatientTestResults(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Return mock data for demo
        return getMockTestResults();
    }

    public List<TestResultResponse> getTestResultsByType(TestType testType, Authentication authentication) {
        return getPatientTestResults(authentication).stream()
                .filter(result -> result.getTestType() == testType)
                .toList();
    }

    @Transactional
    public TestResultResponse createTestResult(TestResultRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        TestResultResponse response = TestResultResponse.builder()
                .id(System.currentTimeMillis())
                .testType(TestType.valueOf(request.getTestType().toUpperCase()))
                .testValue(request.getTestValue())
                .unit(request.getUnit())
                .referenceRange(request.getReferenceRange())
                .testDate(request.getTestDate())
                .isNormal(request.getIsNormal())
                .trend(request.getTrend())
                .doctorNotes(request.getDoctorNotes())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Created test result for user: {}, type: {}", user.getEmail(), request.getTestType());
        
        return response;
    }

    private List<TestResultResponse> getMockTestResults() {
        return Arrays.asList(
            TestResultResponse.builder()
                .id(1L)
                .testType(TestType.CD4_COUNT)
                .testValue("650")
                .unit("cells/mm³")
                .referenceRange("500-1600")
                .testDate(LocalDateTime.of(2024, 12, 15, 9, 0))
                .isNormal(true)
                .trend("UP")
                .previousValue("580")
                .doctorNotes("Kết quả tốt, tiếp tục điều trị hiện tại")
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .build(),
                
            TestResultResponse.builder()
                .id(2L)
                .testType(TestType.VIRAL_LOAD)
                .testValue("Không phát hiện")
                .unit("copies/ml")
                .referenceRange("<50")
                .testDate(LocalDateTime.of(2024, 12, 15, 9, 0))
                .isNormal(true)
                .trend("STABLE")
                .previousValue("Không phát hiện")
                .doctorNotes("Viral load ổn định, điều trị hiệu quả")
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .build(),
                
            TestResultResponse.builder()
                .id(3L)
                .testType(TestType.HEMOGLOBIN)
                .testValue("13.2")
                .unit("g/dL")
                .referenceRange("12.0-15.5")
                .testDate(LocalDateTime.of(2024, 12, 15, 9, 0))
                .isNormal(true)
                .trend("STABLE")
                .previousValue("13.1")
                .doctorNotes("Hemoglobin bình thường")
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .build(),
                
            TestResultResponse.builder()
                .id(4L)
                .testType(TestType.CREATININE)
                .testValue("0.9")
                .unit("mg/dL")
                .referenceRange("0.6-1.2")
                .testDate(LocalDateTime.of(2024, 12, 15, 9, 0))
                .isNormal(true)
                .trend("DOWN")
                .previousValue("1.0")
                .doctorNotes("Chức năng thận ổn định")
                .createdAt(LocalDateTime.of(2024, 12, 15, 10, 0))
                .build(),
                
            // Historical data
            TestResultResponse.builder()
                .id(5L)
                .testType(TestType.CD4_COUNT)
                .testValue("630")
                .unit("cells/mm³")
                .referenceRange("500-1600")
                .testDate(LocalDateTime.of(2024, 11, 15, 9, 0))
                .isNormal(true)
                .trend("UP")
                .previousValue("610")
                .doctorNotes("Tiến triển tốt")
                .createdAt(LocalDateTime.of(2024, 11, 15, 10, 0))
                .build(),
                
            TestResultResponse.builder()
                .id(6L)
                .testType(TestType.CD4_COUNT)
                .testValue("610")
                .unit("cells/mm³")
                .referenceRange("500-1600")
                .testDate(LocalDateTime.of(2024, 10, 15, 9, 0))
                .isNormal(true)
                .trend("UP")
                .previousValue("595")
                .doctorNotes("Cải thiện tốt")
                .createdAt(LocalDateTime.of(2024, 10, 15, 10, 0))
                .build()
        );
    }
}
