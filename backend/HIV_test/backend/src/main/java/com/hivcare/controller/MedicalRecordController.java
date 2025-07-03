package com.hivcare.controller;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.MedicalRecord;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.service.MedicalRecordService;
import com.hivcare.service.PatientService;
import com.hivcare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class MedicalRecordController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<MedicalRecord>> getAllMedicalRecords(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        try {
            logger.debug("Lấy danh sách hồ sơ y tế với từ khóa: {}", search);
            Page<MedicalRecord> records = medicalRecordService.getAllMedicalRecords(search, pageable);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách hồ sơ y tế: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Page.empty());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable @NotNull Long id) {
        try {
            logger.debug("Lấy hồ sơ y tế theo ID: {}", id);
            return medicalRecordService.getMedicalRecordById(id)
                    .map(record -> {
                        logger.debug("Đã tìm thấy hồ sơ y tế với ID: {}", id);
                        return ResponseEntity.ok(record);
                    })
                    .orElseGet(() -> {
                        logger.debug("Không tìm thấy hồ sơ y tế với ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế theo ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/number/{recordNumber}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MedicalRecord> getMedicalRecordByNumber(
            @PathVariable @NotBlank String recordNumber) {
        try {
            logger.debug("Lấy hồ sơ y tế theo mã: {}", recordNumber);
            return medicalRecordService.getMedicalRecordByNumber(recordNumber)
                    .map(record -> {
                        logger.debug("Đã tìm thấy hồ sơ y tế với mã: {}", recordNumber);
                        return ResponseEntity.ok(record);
                    })
                    .orElseGet(() -> {
                        logger.debug("Không tìm thấy hồ sơ y tế với mã: {}", recordNumber);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế theo mã {}: {}", recordNumber, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByPatient(
            @PathVariable @NotNull Long patientId) {
        try {
            logger.debug("Lấy danh sách hồ sơ y tế của bệnh nhân: {}", patientId);
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByPatientId(patientId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế của bệnh nhân {}", records.size(), patientId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế của bệnh nhân {}: {}", patientId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByDoctor(
            @PathVariable @NotNull Long doctorId) {
        try {
            logger.debug("Lấy danh sách hồ sơ y tế của bác sĩ: {}", doctorId);
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByDoctorId(doctorId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế của bác sĩ {}", records.size(), doctorId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế của bác sĩ {}: {}", doctorId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createMedicalRecord(@Valid @RequestBody MedicalRecord record) {
        try {
            logger.debug("Tạo hồ sơ y tế mới");
            MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(record);
            logger.debug("Đã tạo hồ sơ y tế mới với ID: {}", savedRecord.getId());
            return ResponseEntity.ok(savedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hồ sơ y tế mới: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi tạo hồ sơ y tế: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody MedicalRecord recordDetails) {
        try {
            logger.debug("Cập nhật hồ sơ y tế với ID: {}", id);
            MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(id, recordDetails);
            logger.debug("Đã cập nhật hồ sơ y tế với ID: {}", id);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hồ sơ y tế với ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi cập nhật hồ sơ y tế: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> deleteMedicalRecord(@PathVariable @NotNull Long id) {
        try {
            logger.debug("Xóa hồ sơ y tế với ID: {}", id);
            medicalRecordService.deleteMedicalRecord(id);
            logger.debug("Đã xóa hồ sơ y tế với ID: {}", id);
            return ResponseEntity.ok()
                    .body(new ApiResponse(true, "Đã xóa hồ sơ y tế thành công"));
        } catch (Exception e) {
            logger.error("Lỗi khi xóa hồ sơ y tế với ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi xóa hồ sơ y tế: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasAnyRole('DOCTOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<MedicalRecord>> getPatientMedicalHistory(
            @PathVariable @NotNull Long patientId) {
        try {
            logger.debug("Lấy lịch sử khám bệnh của bệnh nhân: {}", patientId);
            List<MedicalRecord> records = medicalRecordService.getPatientMedicalHistory(patientId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế trong lịch sử của bệnh nhân {}", records.size(), patientId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy lịch sử khám bệnh của bệnh nhân {}: {}", patientId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> getMedicalRecordStats(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        try {
            logger.debug("Lấy thống kê hồ sơ y tế cho bệnh nhân {} và bác sĩ {}", patientId, doctorId);
            Map<String, Long> stats = new HashMap<>();
            stats.put("patientRecords", patientId != null ? medicalRecordService.countMedicalRecordsByPatient(patientId) : 0);
            stats.put("doctorRecords", doctorId != null ? medicalRecordService.countMedicalRecordsByDoctor(doctorId) : 0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thống kê hồ sơ y tế: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi lấy thống kê hồ sơ y tế: " + e.getMessage()));
        }
    }

    @GetMapping("/my-records")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyMedicalRecords() {
        try {
            logger.debug("Lấy danh sách hồ sơ y tế của bệnh nhân hiện tại");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.getUserByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            Optional<Patient> patient = patientService.getPatientByUserId(user.getId());
            if (!patient.isPresent()) {
                logger.debug("Không tìm thấy thông tin bệnh nhân cho người dùng: {}", user.getId());
                return ResponseEntity.notFound().build();
            }
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByPatientId(patient.get().getId());
            logger.debug("Đã tìm thấy {} hồ sơ y tế của bệnh nhân", records.size());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế của bệnh nhân hiện tại: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi lấy hồ sơ y tế: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecord> getLatestMedicalRecordByPatient(
            @PathVariable @NotNull Long patientId) {
        try {
            logger.debug("Lấy hồ sơ y tế mới nhất của bệnh nhân: {}", patientId);
            return medicalRecordService.getLatestMedicalRecordByPatient(patientId)
                    .map(record -> {
                        logger.debug("Đã tìm thấy hồ sơ y tế mới nhất của bệnh nhân {}", patientId);
                        return ResponseEntity.ok(record);
                    })
                    .orElseGet(() -> {
                        logger.debug("Không tìm thấy hồ sơ y tế nào của bệnh nhân {}", patientId);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế mới nhất của bệnh nhân {}: {}", patientId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<?> getMedicalRecordsByDateRange(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.debug("Lấy danh sách hồ sơ y tế từ {} đến {}", startDate, endDate);
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Ngày kết thúc không thể trước ngày bắt đầu"));
            }
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByDateRange(startDate, endDate);
            logger.debug("Đã tìm thấy {} hồ sơ y tế trong khoảng thời gian", records.size());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách hồ sơ y tế theo khoảng thời gian: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi lấy danh sách hồ sơ y tế: " + e.getMessage()));
        }
    }

    @PostMapping("/from-appointment")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createFromAppointment(
            @RequestParam @NotNull Long patientId,
            @RequestParam @NotNull Long doctorId,
            @RequestParam(required = false) String chiefComplaint) {
        try {
            logger.debug("Tạo hồ sơ y tế từ lịch hẹn cho bệnh nhân {} và bác sĩ {}", patientId, doctorId);
            MedicalRecord savedRecord = medicalRecordService.createFromAppointment(patientId, doctorId, chiefComplaint);
            logger.debug("Đã tạo hồ sơ y tế mới với ID: {}", savedRecord.getId());
            return ResponseEntity.ok(savedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hồ sơ y tế từ lịch hẹn: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi tạo hồ sơ y tế từ lịch hẹn: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/diagnosis")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> addDiagnosis(
            @PathVariable @NotNull Long id,
            @RequestParam @NotBlank String diagnosis,
            @RequestParam(required = false) String treatmentPlan) {
        try {
            logger.debug("Thêm chẩn đoán cho hồ sơ y tế {}: {}", id, diagnosis);
            MedicalRecord updatedRecord = medicalRecordService.addDiagnosis(id, diagnosis, treatmentPlan);
            logger.debug("Đã thêm chẩn đoán cho hồ sơ y tế {}", id);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm chẩn đoán cho hồ sơ y tế {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi thêm chẩn đoán: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/prescription")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> addPrescription(
            @PathVariable @NotNull Long id,
            @RequestParam @NotBlank String prescriptions) {
        try {
            logger.debug("Thêm đơn thuốc cho hồ sơ y tế {}", id);
            MedicalRecord updatedRecord = medicalRecordService.addPrescription(id, prescriptions);
            logger.debug("Đã thêm đơn thuốc cho hồ sơ y tế {}", id);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm đơn thuốc cho hồ sơ y tế {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi thêm đơn thuốc: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/follow-up")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> addFollowUpInstructions(
            @PathVariable @NotNull Long id,
            @RequestParam @NotBlank String instructions) {
        try {
            logger.debug("Thêm hướng dẫn theo dõi cho hồ sơ y tế {}", id);
            MedicalRecord updatedRecord = medicalRecordService.addFollowUpInstructions(id, instructions);
            logger.debug("Đã thêm hướng dẫn theo dõi cho hồ sơ y tế {}", id);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm hướng dẫn theo dõi cho hồ sơ y tế {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi khi thêm hướng dẫn theo dõi: " + e.getMessage()));
        }
    }
}
