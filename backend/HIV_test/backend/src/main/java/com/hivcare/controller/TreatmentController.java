package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.Treatment;
import com.hivcare.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<Page<Treatment>> getAllTreatments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Treatment> treatments = treatmentService.getAllTreatments(pageable);
        
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<Treatment> getTreatmentById(@PathVariable Long id) {
        return treatmentService.getTreatmentById(id)
                .map(treatment -> ResponseEntity.ok().body(treatment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<Treatment> getTreatmentByCode(@PathVariable String code) {
        return treatmentService.getTreatmentByCode(code)
                .map(treatment -> ResponseEntity.ok().body(treatment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<List<Treatment>> getTreatmentsByPatient(@PathVariable Long patientId) {
        List<Treatment> treatments = treatmentService.getTreatmentsByPatient(patientId);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<List<Treatment>> getActiveTreatmentsByPatient(@PathVariable Long patientId) {
        List<Treatment> treatments = treatmentService.getActiveTreatmentsByPatient(patientId);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Treatment>> getTreatmentsByDoctor(@PathVariable Long doctorId) {
        List<Treatment> treatments = treatmentService.getTreatmentsByDoctor(doctorId);
        return ResponseEntity.ok(treatments);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<List<Treatment>> getExpiredTreatments() {
        List<Treatment> expiredTreatments = treatmentService.getExpiredTreatments();
        return ResponseEntity.ok(expiredTreatments);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createTreatment(@RequestBody Treatment treatment) {
        try {
            Treatment savedTreatment = treatmentService.createTreatment(treatment);
            return ResponseEntity.ok(savedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo phác đồ điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateTreatment(@PathVariable Long id, @RequestBody Treatment treatmentDetails) {
        try {
            Treatment updatedTreatment = treatmentService.updateTreatment(id, treatmentDetails);
            return ResponseEntity.ok(updatedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật phác đồ điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> completeTreatment(@PathVariable Long id) {
        try {
            Treatment completedTreatment = treatmentService.completeTreatment(id);
            return ResponseEntity.ok(completedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi hoàn thành điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/pause")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> pauseTreatment(@PathVariable Long id, @RequestParam String reason) {
        try {
            Treatment pausedTreatment = treatmentService.pauseTreatment(id, reason);
            return ResponseEntity.ok(pausedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạm dừng điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/resume")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> resumeTreatment(@PathVariable Long id) {
        try {
            Treatment resumedTreatment = treatmentService.resumeTreatment(id);
            return ResponseEntity.ok(resumedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tiếp tục điều trị: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/customize-arv")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> customizeArvProtocol(@PathVariable Long id, @RequestParam String customInstructions) {
        try {
            Treatment customizedTreatment = treatmentService.customizeArvProtocol(id, customInstructions);
            return ResponseEntity.ok(customizedTreatment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tùy chỉnh phác đồ ARV: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTreatment(@PathVariable Long id) {
        try {
            treatmentService.deleteTreatment(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa phác đồ điều trị thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa phác đồ điều trị: " + e.getMessage()));
        }
    }
}
