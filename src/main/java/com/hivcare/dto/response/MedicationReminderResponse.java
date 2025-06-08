package com.hivcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationReminderResponse {
    
    private Long id;
    private String medicationName;
    private String dosage;
    private LocalTime reminderTime;
    private Boolean isActive;
    private LocalDateTime lastTaken;
    private LocalDateTime nextRefillDate;
    private String instructions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
