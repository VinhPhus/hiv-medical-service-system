package com.hivcare.dto.response;

import com.hivcare.enums.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {
    
    private Long id;
    private TestType testType;
    private String testValue;
    private String unit;
    private String referenceRange;
    private LocalDateTime testDate;
    private Boolean isNormal;
    private String trend;
    private String previousValue;
    private String doctorNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
