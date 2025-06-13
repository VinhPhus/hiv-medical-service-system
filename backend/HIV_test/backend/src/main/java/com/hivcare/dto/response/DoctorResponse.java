package com.hivcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String bio;
    private String avatar;
    private Double rating;
    private Integer totalReviews;
    private Boolean availableToday;
    private List<String> availableDays;
    private LocalTime workingHoursStart;
    private LocalTime workingHoursEnd;
    private String clinicAddress;
    private Boolean isOnlineConsultationAvailable;
    private Double consultationFee;
    private String languages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
