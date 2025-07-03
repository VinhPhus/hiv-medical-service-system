package com.hivcare.controller;

import com.hivcare.entity.Patient;
import com.hivcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patient")
public class PatientWebController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public String listPatients(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Patient.TreatmentStatus treatmentStatus,
            @RequestParam(required = false) Patient.Status status
    ) {
        try {
            // Tạo pageable với sắp xếp theo thời gian tạo giảm dần
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            // Lấy danh sách bệnh nhân với phân trang và lọc
            Page<Patient> patients;
            if (search != null && !search.trim().isEmpty()) {
                // Nếu có từ khóa tìm kiếm
                patients = patientService.searchPatients(search, pageable);
            } else if (treatmentStatus != null) {
                // Nếu có lọc theo trạng thái điều trị
                patients = new PageImpl<>(patientService.getPatientsByTreatmentStatus(treatmentStatus), pageable, 
                    patientService.countPatientsByTreatmentStatus(treatmentStatus));
            } else if (status != null) {
                // Nếu có lọc theo trạng thái
                patients = new PageImpl<>(patientService.getPatientsByStatus(status), pageable,
                    patientService.countPatientsByStatus(status));
            } else {
                // Lấy tất cả bệnh nhân
                patients = patientService.getAllPatients(pageable);
            }

            // Thêm dữ liệu vào model
            model.addAttribute("patients", patients);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", patients.getTotalPages());
            model.addAttribute("totalItems", patients.getTotalElements());
            model.addAttribute("search", search);
            model.addAttribute("treatmentStatus", treatmentStatus);
            model.addAttribute("status", status);
            
            // Thêm danh sách các trạng thái để hiển thị trong dropdown
            model.addAttribute("treatmentStatuses", Patient.TreatmentStatus.values());
            model.addAttribute("statuses", Patient.Status.values());

            // Thống kê
            model.addAttribute("totalPatients", patients.getTotalElements());
            model.addAttribute("activePatients", patientService.countPatientsByTreatmentStatus(Patient.TreatmentStatus.ACTIVE));
            model.addAttribute("completedPatients", patientService.countPatientsByTreatmentStatus(Patient.TreatmentStatus.COMPLETED));
            model.addAttribute("newPatients", patientService.countNewPatientsInLast7Days());

            return "patient/list";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách bệnh nhân: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public String viewPatient(@PathVariable Long id, Model model) {
        try {
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));
            model.addAttribute("patient", patient);
            return "patient/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin bệnh nhân: " + e.getMessage());
            return "error/500";
        }
    }
} 