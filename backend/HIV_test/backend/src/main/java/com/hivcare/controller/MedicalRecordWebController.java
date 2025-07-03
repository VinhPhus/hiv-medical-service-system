package com.hivcare.controller;

import com.hivcare.entity.MedicalRecord;
import com.hivcare.entity.Patient;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.User;
import com.hivcare.service.MedicalRecordService;
import com.hivcare.service.PatientService;
import com.hivcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

@Controller
@RequestMapping("/medicalRecord")
@Validated
public class MedicalRecordWebController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordWebController.class);

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    private boolean isPatientAuthorized(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            if (user.getRole() == User.Role.PATIENT) {
                Optional<Patient> patient = patientService.getPatientByUserId(user.getId());
                return patient.isPresent() && patient.get().getId().equals(patientId);
            }
        }
        return true; // Non-patient users are authorized
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'STAFF', 'PATIENT')")
    public String showMedicalRecordList(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            
            List<MedicalRecord> records;
            if (user.getRole() == User.Role.PATIENT) {
                Optional<Patient> patient = patientService.getPatientByUserId(user.getId());
                if (patient.isPresent()) {
                    records = medicalRecordService.getMedicalRecordsByPatientId(patient.get().getId());
                } else {
                    throw new RuntimeException("Không tìm thấy thông tin bệnh nhân");
                }
            } else {
                records = medicalRecordService.getAllMedicalRecords();
            }
            
            model.addAttribute("medicalRecords", records);
            model.addAttribute("userRole", user.getRole());
            return "medicalRecord/list";
        } catch (Exception e) {
            logger.error("Error loading medical record list: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách hồ sơ y tế: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/form")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public String showMedicalRecordForm(Model model) {
        try {
            if (!model.containsAttribute("medicalRecord")) {
                MedicalRecord medicalRecord = new MedicalRecord();
                medicalRecord.setVisitDate(LocalDateTime.now());
                
                // Set current doctor only if user is a DOCTOR
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = (User) auth.getPrincipal();
                if (user.getRole() == User.Role.DOCTOR) {
                    Doctor doctor = doctorService.getDoctorByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                    medicalRecord.setDoctor(doctor);
                }
                
                model.addAttribute("medicalRecord", medicalRecord);
            }
            
            // Lấy danh sách bệnh nhân và hồ sơ y tế mới nhất
            List<Patient> patients = patientService.getAllPatients();
            List<MedicalRecord> latestRecords = medicalRecordService.getLatestRecordsForAllPatients();
            
            // Tạo map để lưu trữ hồ sơ mới nhất cho mỗi bệnh nhân
            Map<Long, MedicalRecord> patientLatestRecords = latestRecords.stream()
                .collect(Collectors.toMap(
                    record -> record.getPatient().getId(),
                    record -> record
                ));
            
            // Gán hồ sơ mới nhất cho từng bệnh nhân
            for (Patient patient : patients) {
                MedicalRecord latestRecord = patientLatestRecords.get(patient.getId());
                patient.setMedicalRecords(latestRecord != null ? Set.of(latestRecord) : Set.of());
            }
            model.addAttribute("patients", patients);
            
            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            model.addAttribute("doctors", doctors);

            return "medicalRecord/form";
        } catch (Exception e) {
            logger.error("Error showing medical record form: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi hiển thị form hồ sơ y tế: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/form/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public String showEditMedicalRecordForm(@PathVariable Long id, Model model) {
        try {
            MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));

            // Verify doctor authorization
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();
            Doctor currentDoctor = doctorService.getDoctorByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));

            if (!medicalRecord.getDoctor().getId().equals(currentDoctor.getId())) {
                model.addAttribute("error", "Bạn không có quyền chỉnh sửa hồ sơ y tế này");
                return "error/403";
            }

            model.addAttribute("medicalRecord", medicalRecord);
            List<Patient> patients = patientService.getAllPatients();
            model.addAttribute("patients", patients);

            return "medicalRecord/form";
        } catch (Exception e) {
            logger.error("Error showing edit form for medical record ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi hiển thị form chỉnh sửa hồ sơ y tế: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public String saveMedicalRecord(
            @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            BindingResult bindingResult,
            @RequestParam(required = false) String action,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin bắt buộc");
                List<Patient> patients = patientService.getAllPatients();
                model.addAttribute("patients", patients);
                List<Doctor> doctors = doctorService.getAllActiveDoctors();
                model.addAttribute("doctors", doctors);
                return "medicalRecord/form";
            }

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) auth.getPrincipal();

            // Check authorization for editing
            if (medicalRecord.getId() != null) {
                MedicalRecord existingRecord = medicalRecordService.getMedicalRecordById(medicalRecord.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + medicalRecord.getId()));
                
                // Only check doctor authorization if current user is a doctor
                if (user.getRole() == User.Role.DOCTOR) {
                    Doctor currentDoctor = doctorService.getDoctorByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                    if (!existingRecord.getDoctor().getId().equals(currentDoctor.getId())) {
                        model.addAttribute("errorMessage", "Bạn không có quyền chỉnh sửa hồ sơ y tế này");
                        List<Patient> patients = patientService.getAllPatients();
                        model.addAttribute("patients", patients);
                        List<Doctor> doctors = doctorService.getAllActiveDoctors();
                        model.addAttribute("doctors", doctors);
                        return "medicalRecord/form";
                    }
                }
            }

            // Set doctor if not already set
            if (medicalRecord.getDoctor() == null && user.getRole() == User.Role.DOCTOR) {
                Doctor doctor = doctorService.getDoctorByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                medicalRecord.setDoctor(doctor);
            }

            // Set next visit date if not provided
            if (medicalRecord.getNextVisitDate() == null) {
                medicalRecord.setNextVisitDate(medicalRecord.getVisitDate().plusMonths(1));
            }

            // Save the record
            if (medicalRecord.getId() != null) {
                medicalRecordService.updateMedicalRecord(medicalRecord.getId(), medicalRecord);
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật hồ sơ y tế thành công");
            } else {
                MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(medicalRecord);
                redirectAttributes.addFlashAttribute("successMessage", "Đã tạo hồ sơ y tế mới thành công");
            }

            return "redirect:/medicalRecord/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<Patient> patients = patientService.getAllPatients();
            model.addAttribute("patients", patients);
            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            model.addAttribute("doctors", doctors);
            return "medicalRecord/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu hồ sơ y tế: " + e.getMessage());
            List<Patient> patients = patientService.getAllPatients();
            model.addAttribute("patients", patients);
            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            model.addAttribute("doctors", doctors);
            return "medicalRecord/form";
        }
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'STAFF', 'PATIENT')")
    public String showMedicalRecordDetail(@PathVariable Long id, Model model) {
        try {
            MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));

            // Check patient authorization
            if (!isPatientAuthorized(medicalRecord.getPatient().getId())) {
                model.addAttribute("error", "Bạn không có quyền xem hồ sơ y tế này");
                return "error/403";
            }

            model.addAttribute("medicalRecord", medicalRecord);
            return "medicalRecord/detail";
        } catch (Exception e) {
            logger.error("Error showing detail for medical record ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi hiển thị chi tiết hồ sơ y tế: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMedicalRecord(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.deleteMedicalRecord(id);
            logger.info("Deleted medical record ID {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hồ sơ y tế thành công");
            return "redirect:/medicalRecord/list";
        } catch (Exception e) {
            logger.error("Error deleting medical record ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa hồ sơ y tế: " + e.getMessage());
            return "redirect:/medicalRecord/list";
        }
    }
} 