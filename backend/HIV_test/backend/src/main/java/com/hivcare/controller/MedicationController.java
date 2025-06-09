package com.hivcare.controller;

import com.hivcare.dto.response.ARVRegimenResponse;
import com.hivcare.dto.response.MedicationReminderResponse;
import com.hivcare.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping("/reminders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<MedicationReminderResponse>> getPatientReminders(Authentication authentication) {
        List<MedicationReminderResponse> reminders = medicationService.getPatientReminders(authentication);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/arv-regimens")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ARVRegimenResponse>> getAvailableARVRegimens() {
        List<ARVRegimenResponse> regimens = medicationService.getAvailableARVRegimens();
        return ResponseEntity.ok(regimens);
    }
}
