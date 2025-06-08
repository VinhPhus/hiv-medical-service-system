package com.hivcare.dto.response;

import com.hivcare.enums.AppointmentStatus;
import com.hivcare.enums.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private Long id;
    private String patientName;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private AppointmentType type;
    private AppointmentStatus status;
    private String reason;
    private String notes;
    private Boolean isOnline;
    private Boolean isAnonymous;
    private String anonymousId;
    private String location;
    private String meetingLink;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
