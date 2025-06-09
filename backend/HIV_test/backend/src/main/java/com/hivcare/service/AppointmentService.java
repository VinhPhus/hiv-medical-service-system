package com.hivcare.service;

import com.hivcare.dto.request.AppointmentRequest;
import com.hivcare.dto.response.AppointmentResponse;
import com.hivcare.entity.Appointment;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.enums.AppointmentStatus;
import com.hivcare.enums.AppointmentType;
import com.hivcare.exception.ResourceNotFoundException;
import com.hivcare.exception.BadRequestException;
import com.hivcare.repository.AppointmentRepository;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public List<AppointmentResponse> getPatientAppointments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // For demo purposes, return mock data
        if (user.getRole().name().equals("CUSTOMER")) {
            return getMockPatientAppointments();
        }
        
        return List.of();
    }

    public List<AppointmentResponse> getDoctorAppointments(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // For demo purposes, return mock data
        if (user.getRole().name().equals("DOCTOR")) {
            return getMockDoctorAppointments();
        }
        
        return List.of();
    }

    public AppointmentResponse createAppointment(AppointmentRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Validate appointment time
        if (request.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book appointment in the past");
        }

        // For demo, create mock appointment
        AppointmentResponse response = AppointmentResponse.builder()
                .id(System.currentTimeMillis())
                .patientName(user.getFullName())
                .doctorName("BS. Nguyễn Văn Minh")
                .appointmentDate(request.getAppointmentDate())
                .type(AppointmentType.valueOf(request.getType().toUpperCase()))
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .isOnline(request.getIsOnline() != null ? request.getIsOnline() : false)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Created appointment for user: {} with doctor: {}", user.getEmail(), request.getDoctorId());
        
        return response;
    }

    public AppointmentResponse createAnonymousAppointment(AppointmentRequest request) {
        String anonymousId = "AC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        AppointmentResponse response = AppointmentResponse.builder()
                .id(System.currentTimeMillis())
                .anonymousId(anonymousId)
                .doctorName("Bác sĩ trực")
                .appointmentDate(request.getAppointmentDate())
                .type(AppointmentType.ANONYMOUS_CONSULTATION)
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .isOnline(true)
                .isAnonymous(true)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Created anonymous appointment with ID: {}", anonymousId);
        
        return response;
    }

    public AppointmentResponse updateAppointmentStatus(Long appointmentId, String status, Authentication authentication) {
        // For demo purposes
        AppointmentResponse response = AppointmentResponse.builder()
                .id(appointmentId)
                .status(AppointmentStatus.valueOf(status.toUpperCase()))
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Updated appointment {} status to: {}", appointmentId, status);
        
        return response;
    }

    private List<AppointmentResponse> getMockPatientAppointments() {
        return Arrays.asList(
            AppointmentResponse.builder()
                .id(1L)
                .patientName("Nguyễn Văn A")
                .doctorName("BS. Nguyễn Văn Minh")
                .appointmentDate(LocalDateTime.of(2024, 12, 25, 14, 0))
                .type(AppointmentType.FOLLOW_UP)
                .status(AppointmentStatus.CONFIRMED)
                .reason("Tái khám định kỳ")
                .isOnline(false)
                .location("Phòng khám 101")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build(),
                
            AppointmentResponse.builder()
                .id(2L)
                .patientName("Nguyễn Văn A")
                .doctorName("BS. Trần Thị Lan")
                .appointmentDate(LocalDateTime.of(2024, 12, 28, 10, 30))
                .type(AppointmentType.COUNSELING)
                .status(AppointmentStatus.CONFIRMED)
                .reason("Tư vấn trực tuyến")
                .isOnline(true)
                .location("Zoom Meeting")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build(),
                
            AppointmentResponse.builder()
                .id(3L)
                .patientName("Nguyễn Văn A")
                .doctorName("BS. Lê Hoàng Nam")
                .appointmentDate(LocalDateTime.of(2025, 1, 2, 15, 30))
                .type(AppointmentType.TEST_RESULTS)
                .status(AppointmentStatus.SCHEDULED)
                .reason("Xét nghiệm kiểm tra")
                .isOnline(false)
                .location("Phòng xét nghiệm")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build()
        );
    }

    private List<AppointmentResponse> getMockDoctorAppointments() {
        return Arrays.asList(
            AppointmentResponse.builder()
                .id(1L)
                .patientName("Bệnh nhân A")
                .doctorName("BS. Nguyễn Văn Minh")
                .appointmentDate(LocalDateTime.now().withHour(9).withMinute(0))
                .type(AppointmentType.FOLLOW_UP)
                .status(AppointmentStatus.COMPLETED)
                .reason("Tái khám")
                .isOnline(false)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build(),
                
            AppointmentResponse.builder()
                .id(2L)
                .patientName("Bệnh nhân B")
                .doctorName("BS. Nguyễn Văn Minh")
                .appointmentDate(LocalDateTime.now().withHour(10).withMinute(30))
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.IN_PROGRESS)
                .reason("Khám mới")
                .isOnline(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build(),
                
            AppointmentResponse.builder()
                .id(3L)
                .patientName("Bệnh nhân C")
                .doctorName("BS. Nguyễn Văn Minh")
                .appointmentDate(LocalDateTime.now().withHour(14).withMinute(0))
                .type(AppointmentType.COUNSELING)
                .status(AppointmentStatus.SCHEDULED)
                .reason("Tư vấn")
                .isOnline(true)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build()
        );
    }
}
