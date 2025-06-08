package com.hivcare.dto.response;

import com.hivcare.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private Long id;
    private String patientCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private LocalDateTime diagnosisDate;
    private LocalDateTime treatmentStartDate;
    private String currentArvRegimen;
    private Double adherenceRate;
    private Integer latestCd4;
    private String latestViralLoad;
    private LocalDateTime nextAppointmentDate;
    private String treatmentNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
