package com.hivcare.dto.request;

import lombok.Data;
import java.time.LocalTime;

@Data
public class MedicationReminderRequest {
    private String title;
    private String medicationName;
    private LocalTime reminderTime;
    private Boolean isDaily;
    private String notes;
}