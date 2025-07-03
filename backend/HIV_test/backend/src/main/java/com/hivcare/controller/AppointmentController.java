package com.hivcare.controller;

import com.hivcare.dto.request.AppointmentRequest;
import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.Appointment;
import com.hivcare.entity.Patient;
import com.hivcare.service.AppointmentService;
import com.hivcare.service.PatientService;
import com.hivcare.service.UserService;
import com.hivcare.security.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Page<Appointment>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Appointment> appointments = appointmentService.getAllAppointments(pageable);
            
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        try {
            return appointmentService.getAppointmentById(id)
                    .map(appointment -> ResponseEntity.ok().body(appointment))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Patient patient = patientService.getPatientByUserId(getUserIdFromUsername(username))
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient.getId());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<Appointment> appointments;
            
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate);
                LocalDateTime end = LocalDateTime.parse(endDate);
                appointments = appointmentService.getDoctorAppointmentsByDate(doctorId, start, end);
            } else {
                appointments = appointmentService.getAppointmentsByDoctor(doctorId);
            }
            
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest,
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu đặt lịch không hợp lệ"));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Patient patient = patientService.getPatientByUserId(getUserIdFromUsername(username))
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            
            // Validate appointment time
            if (appointmentRequest.getAppointmentDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Thời gian đặt lịch phải là thời gian trong tương lai"));
            }

            // Validate working hours (8:00 - 17:00)
            int hour = appointmentRequest.getAppointmentDate().getHour();
            if (hour < 8 || hour >= 17) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Thời gian đặt lịch phải trong khoảng 8:00 - 17:00"));
            }
            
            Appointment appointment = appointmentService.bookAppointment(patient.getId(), appointmentRequest);
            return ResponseEntity.ok(appointment);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi đặt lịch hẹn: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, 
                                             @Valid @RequestBody Appointment appointmentDetails,
                                             BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dữ liệu cập nhật không hợp lệ"));
            }

            // Validate appointment time
            if (appointmentDetails.getAppointmentDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Thời gian cập nhật phải là thời gian trong tương lai"));
            }

            // Validate working hours (8:00 - 17:00)
            int hour = appointmentDetails.getAppointmentDate().getHour();
            if (hour < 8 || hour >= 17) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Thời gian cập nhật phải trong khoảng 8:00 - 17:00"));
            }

            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentDetails);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật lịch hẹn: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @RequestParam String reason) {
        try {
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Vui lòng cung cấp lý do hủy lịch hẹn"));
            }

            Appointment cancelledAppointment = appointmentService.cancelAppointment(id, reason);
            return ResponseEntity.ok(cancelledAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi hủy lịch hẹn: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa lịch hẹn thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa lịch hẹn: " + e.getMessage()));
        }
    }

    // Helper method to get user ID from username
    private Long getUserIdFromUsername(String username) {
        return userService.getUserIdByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
