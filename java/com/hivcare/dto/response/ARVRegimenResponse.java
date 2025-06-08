package com.hivcare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ARVRegimenResponse {
    
    private Long id;
    private String name;
    private String description;
    private List<String> components;
    private List<String> suitableFor;
    private List<String> contraindications;
    private List<String> sideEffects;
    private String dosageInstructions;
    private Boolean isPreferred;
    private String notes;
}
