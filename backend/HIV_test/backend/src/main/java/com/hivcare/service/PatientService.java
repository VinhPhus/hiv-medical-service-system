package com.hivcare.service;

import com.hivcare.dto.response.PatientResponse;
import com.hivcare.entity.User;
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
public class PatientService {

    public PatientResponse getPatientProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Return mock patient data for demo
        return PatientResponse.builder()
                .id(1L)
                .patientCode("PAT001")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .diagnosisDate(LocalDateTime.of(2020, 1, 15, 0, 0))
                .treatmentStartDate(LocalDateTime.of(2020, 1, 20, 0, 0))
                .currentArvRegimen("TDF + 3TC + DTG")
                .adherenceRate(95.0)
                .latestCd4(650)
                .latestViralLoad("Không phát hiện")
                .nextAppointmentDate(LocalDateTime.of(2024, 12, 25, 14, 0))
                .treatmentNotes("Bệnh nhân tuân thủ điều trị tốt, viral load ổn định")
                .build();
    }

    public List<PatientResponse> getAllPatients() {
        // Return mock data for demo
        return Arrays.asList(
            PatientResponse.builder()
                .id(1L)
                .patientCode("PAT001")
                .fullName("Nguyễn Văn A")
                .email("patient@hiv.care")
                .currentArvRegimen("TDF + 3TC + DTG")
                .adherenceRate(95.0)
                .latestCd4(650)
                .latestViralLoad("Không phát hiện")
                .nextAppointmentDate(LocalDateTime.of(2024, 12, 25, 14, 0))
                .build(),
                
            PatientResponse.builder()
                .id(2L)
                .patientCode("PAT002")
                .fullName("Trần Thị B")
                .email("patient2@example.com")
                .currentArvRegimen("ABC + 3TC + DTG")
                .adherenceRate(88.0)
                .latestCd4(520)
                .latestViralLoad("45")
                .nextAppointmentDate(LocalDateTime.of(2024, 12, 28, 10, 0))
                .build(),
                
            PatientResponse.builder()
                .id(3L)
                .patientCode("PAT003")
                .fullName("Lê Văn C")
                .email("patient3@example.com")
                .currentArvRegimen("TDF + FTC + EFV")
                .adherenceRate(92.0)
                .latestCd4(480)
                .latestViralLoad("Không phát hiện")
                .nextAppointmentDate(LocalDateTime.of(2025, 1, 5, 9, 0))
                .build()
        );
    }
}
