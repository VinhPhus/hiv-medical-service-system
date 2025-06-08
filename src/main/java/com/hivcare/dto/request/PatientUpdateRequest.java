package com.hivcare.dto.request;

import lombok.Data;

@Data
public class PatientUpdateRequest {
    private String currentArvRegimen;
    private Double adherenceRate;
    private String emergencyContact;
    private String pharmacy;
    private String insuranceProvider;
    private String notes;
}