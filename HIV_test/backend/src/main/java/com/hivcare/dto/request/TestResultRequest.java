package com.hivcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultRequest {
    
    @NotBlank(message = "Test type is required")
    private String testType;
    
    @NotBlank(message = "Test value is required")
    private String testValue;
    
    private String unit;
    
    private String referenceRange;
    
    @NotNull(message = "Test date is required")
    private LocalDateTime testDate;
    
    private Boolean isNormal;
    
    private String trend;
    
    private String previousValue;
    
    private String doctorNotes;
}
