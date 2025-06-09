package com.hivcare.controller;

import com.hivcare.dto.request.AppointmentRequest;
import com.hivcare.dto.response.AppointmentResponse;
import com.hivcare.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        if (role.equals("ROLE_DOCTOR")) {
            return ResponseEntity.ok(appointmentService.getDoctorAppointments(authentication));
        } else {
            return ResponseEntity.ok(appointmentService.getPatientAppointments(authentication));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) {
        AppointmentResponse response = appointmentService.createAppointment(request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/anonymous")
    public ResponseEntity<AppointmentResponse> createAnonymousAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAnonymousAppointment(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        AppointmentResponse response = appointmentService.updateAppointmentStatus(id, status, authentication);
        return ResponseEntity.ok(response);
    }
}
