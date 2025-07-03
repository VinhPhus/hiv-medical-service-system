package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.Patient;
import com.hivcare.service.PatientService;
import com.hivcare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    private Long getUserIdFromUsername(String username) {
        return userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username))
                .getId();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<?> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Patient> patients;
            if (search != null && !search.trim().isEmpty()) {
                patients = patientService.searchPatients(search, pageable);
            } else {
                patients = patientService.getAllPatients(pageable);
            }
            
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy danh sách bệnh nhân: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            return patientService.getPatientById(id)
                    .map(patient -> ResponseEntity.ok().body(patient))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy thông tin bệnh nhân: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<?> getPatientByCode(@PathVariable String code) {
        try {
            return patientService.getPatientByCode(code)
                    .map(patient -> ResponseEntity.ok().body(patient))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy thông tin bệnh nhân: " + e.getMessage()));
        }
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            return patientService.getPatientByUserId(getUserIdFromUsername(username))
                    .map(patient -> ResponseEntity.ok().body(patient))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy thông tin hồ sơ: " + e.getMessage()));
        }
    }

    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getPatientsByDoctor(@PathVariable Long doctorId) {
        try {
            List<Patient> patients = patientService.getPatientsByDoctor(doctorId);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy danh sách bệnh nhân theo bác sĩ: " + e.getMessage()));
        }
    }

    @GetMapping("/by-treatment-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<?> getPatientsByTreatmentStatus(@RequestParam Patient.TreatmentStatus status) {
        try {
            List<Patient> patients = patientService.getPatientsByTreatmentStatus(status);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi lấy danh sách bệnh nhân theo trạng thái điều trị: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            Patient savedPatient = patientService.createPatient(patient);
            return ResponseEntity.ok(savedPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo hồ sơ bệnh nhân: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        try {
            Patient updatedPatient = patientService.updatePatient(id, patientDetails);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật hồ sơ bệnh nhân: " + e.getMessage()));
        }
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateMyProfile(@RequestBody Patient patientDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Patient updatedPatient = patientService.updatePatientProfile(getUserIdFromUsername(username), patientDetails);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật hồ sơ: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/start-treatment")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> startHivTreatment(@PathVariable Long id, @RequestParam LocalDate treatmentStartDate) {
        try {
            Patient updatedPatient = patientService.startHivTreatment(id, treatmentStartDate);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi bắt đầu điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/treatment-status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateTreatmentStatus(@PathVariable Long id, @RequestParam Patient.TreatmentStatus status) {
        try {
            Patient updatedPatient = patientService.updateTreatmentStatus(id, status);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật trạng thái điều trị: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa hồ sơ bệnh nhân thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa hồ sơ bệnh nhân: " + e.getMessage()));
        }
    }
}
