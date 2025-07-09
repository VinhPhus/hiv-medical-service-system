package com.hivcare.service;

import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.entity.Doctor;
import com.hivcare.repository.PatientRepository;
import com.hivcare.repository.UserRepository;
import com.hivcare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        logger.debug("Đang lấy danh sách tất cả bệnh nhân");
        try {
            List<Patient> patients = patientRepository.findAllWithDetails();
            logger.debug("Đã lấy được {} bệnh nhân", patients.size());
            // Trigger lazy loading of user data
            patients.forEach(patient -> {
                if (patient.getUser() != null) {
                    patient.getUser().getFullName(); // Force loading of user data
                }
            });
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân", e);
        }
    }

    public List<Patient> getAllActivePatients() {
        logger.debug("Đang lấy danh sách bệnh nhân đang hoạt động");
        try {
            logger.debug("Gọi repository.findByStatus với status={}", Patient.Status.ACTIVE);
            List<Patient> patients = patientRepository.findByStatus(Patient.Status.ACTIVE);
            if (patients.isEmpty()) {
                logger.warn("Không tìm thấy bệnh nhân nào có trạng thái ACTIVE");
            } else {
                logger.debug("Đã lấy được {} bệnh nhân đang hoạt động", patients.size());
                patients.forEach(patient -> 
                    logger.debug("Bệnh nhân: ID={}, Code={}, Tên={}", 
                        patient.getId(), 
                        patient.getPatientCode(),
                        patient.getUser() != null ? patient.getUser().getFullName() : "N/A")
                );
            }
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân đang hoạt động: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân đang hoạt động", e);
        }
    }

    public Page<Patient> getAllPatients(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable không được để trống");
        }
        logger.debug("Đang lấy danh sách bệnh nhân theo trang {}", pageable);
        try {
            Page<Patient> patients = patientRepository.findAllWithUser(pageable);
            logger.debug("Đã lấy được {} bệnh nhân trong trang", patients.getNumberOfElements());
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân theo trang", e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân theo trang", e);
        }
    }

    public Optional<Patient> getPatientById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Đang tìm bệnh nhân với ID: {}", id);
        try {
            Optional<Patient> patient = patientRepository.findById(id);
            if (patient.isPresent()) {
                logger.debug("Đã tìm thấy bệnh nhân với ID: {}", id);
            } else {
                logger.debug("Không tìm thấy bệnh nhân với ID: {}", id);
            }
            return patient;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bệnh nhân với ID: {}", id, e);
            throw new RuntimeException("Không thể tìm bệnh nhân", e);
        }
    }

    public Optional<Patient> getPatientByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bệnh nhân không được để trống");
        }
        logger.debug("Đang tìm bệnh nhân với mã: {}", code);
        try {
            Optional<Patient> patient = patientRepository.findByPatientCode(code);
            if (patient.isPresent()) {
                logger.debug("Đã tìm thấy bệnh nhân với mã: {}", code);
            } else {
                logger.debug("Không tìm thấy bệnh nhân với mã: {}", code);
            }
            return patient;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bệnh nhân với mã: {}", code, e);
            throw new RuntimeException("Không thể tìm bệnh nhân theo mã", e);
        }
    }

    public Optional<Patient> getPatientByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }
        logger.debug("Đang tìm bệnh nhân với ID người dùng: {}", userId);
        try {
            Optional<Patient> patient = patientRepository.findByUserId(userId);
            if (patient.isPresent()) {
                logger.debug("Đã tìm thấy bệnh nhân với ID người dùng: {}", userId);
            } else {
                logger.debug("Không tìm thấy bệnh nhân với ID người dùng: {}", userId);
            }
            return patient;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bệnh nhân với ID người dùng: {}", userId, e);
            throw new RuntimeException("Không thể tìm bệnh nhân theo ID người dùng", e);
        }
    }

    public Page<Patient> searchPatients(String searchTerm, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable không được để trống");
        }
        logger.debug("Đang tìm kiếm bệnh nhân với từ khóa: {}", searchTerm);
        try {
            Page<Patient> patients = patientRepository.findBySearchTerm(searchTerm, pageable);
            logger.debug("Đã tìm thấy {} bệnh nhân", patients.getTotalElements());
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm bệnh nhân: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tìm kiếm bệnh nhân", e);
        }
    }

    public List<Patient> getPatientsByStatus(Patient.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không được để trống");
        }
        logger.debug("Đang lấy danh sách bệnh nhân theo trạng thái: {}", status);
        try {
            List<Patient> patients = patientRepository.findByStatus(status);
            logger.debug("Đã tìm thấy {} bệnh nhân với trạng thái {}", patients.size(), status);
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân theo trạng thái: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân theo trạng thái", e);
        }
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Thông tin bệnh nhân không được để trống");
        }
        logger.debug("Đang tạo bệnh nhân mới");
        try {
            // Validate required fields
            if (patient.getUser() == null) {
                throw new IllegalArgumentException("Thông tin người dùng không được để trống");
            }
            
            // Check if patient code already exists
            if (patient.getPatientCode() != null && 
                patientRepository.findByPatientCode(patient.getPatientCode()).isPresent()) {
                throw new IllegalArgumentException("Mã bệnh nhân đã tồn tại");
            }

            Patient savedPatient = patientRepository.save(patient);
            logger.debug("Đã tạo bệnh nhân mới với ID: {}", savedPatient.getId());
            return savedPatient;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo bệnh nhân mới: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo bệnh nhân mới", e);
        }
    }

    @Transactional
    public Patient updatePatient(Long id, Patient patientDetails) {
        if (id == null || patientDetails == null) {
            throw new IllegalArgumentException("ID và thông tin bệnh nhân không được để trống");
        }
        logger.debug("Đang cập nhật bệnh nhân với ID: {}", id);
        try {
            Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

            // Cập nhật thông tin
            existingPatient.setDateOfBirth(patientDetails.getDateOfBirth());
            existingPatient.setGender(patientDetails.getGender());
            existingPatient.setAddress(patientDetails.getAddress());
            existingPatient.setEmergencyContact(patientDetails.getEmergencyContact());
            existingPatient.setEmergencyPhone(patientDetails.getEmergencyPhone());
            existingPatient.setHivDiagnosisDate(patientDetails.getHivDiagnosisDate());
            existingPatient.setTreatmentStartDate(patientDetails.getTreatmentStartDate());
            existingPatient.setTreatmentStatus(patientDetails.getTreatmentStatus());
            existingPatient.setInsuranceNumber(patientDetails.getInsuranceNumber());
            existingPatient.setNotes(patientDetails.getNotes());
            existingPatient.setStatus(patientDetails.getStatus());

            // Cập nhật bác sĩ nếu có
            if (patientDetails.getDoctor() != null) {
                Doctor doctor = doctorRepository.findById(patientDetails.getDoctor().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + patientDetails.getDoctor().getId()));
                existingPatient.setDoctor(doctor);
            }

            Patient updatedPatient = patientRepository.save(existingPatient);
            logger.debug("Đã cập nhật bệnh nhân với ID: {}", id);
            return updatedPatient;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật bệnh nhân với ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật bệnh nhân", e);
        }
    }

    @Transactional
    public void deletePatient(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Đang xóa bệnh nhân với ID: {}", id);
        try {
            if (!patientRepository.existsById(id)) {
                throw new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id);
            }
            patientRepository.deleteById(id);
            logger.debug("Đã xóa bệnh nhân với ID: {}", id);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa bệnh nhân với ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Không thể xóa bệnh nhân", e);
        }
    }

    public long getTotalPatients() {
        logger.debug("Đang đếm tổng số bệnh nhân");
        try {
            long total = patientRepository.count();
            logger.debug("Tổng số bệnh nhân: {}", total);
            return total;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm tổng số bệnh nhân: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đếm tổng số bệnh nhân", e);
        }
    }

    public long countPatientsByStatus(Patient.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không được để trống");
        }
        logger.debug("Đang đếm số bệnh nhân theo trạng thái: {}", status);
        try {
            List<Patient> patients = patientRepository.findByStatus(status);
            long count = patients.size();
            logger.debug("Số bệnh nhân có trạng thái {}: {}", status, count);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số bệnh nhân theo trạng thái: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đếm số bệnh nhân theo trạng thái", e);
        }
    }

    @Transactional
    public void save(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Thông tin bệnh nhân không được để trống");
        }
        logger.debug("Đang lưu thông tin bệnh nhân");
        try {
            patientRepository.save(patient);
            logger.debug("Đã lưu thông tin bệnh nhân thành công");
        } catch (Exception e) {
            logger.error("Lỗi khi lưu thông tin bệnh nhân: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu thông tin bệnh nhân", e);
        }
    }

    public List<Patient> getPatientsByDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("ID bác sĩ không được để trống");
        }
        logger.debug("Đang lấy danh sách bệnh nhân của bác sĩ với ID: {}", doctorId);
        try {
            List<Patient> patients = patientRepository.findByDoctorId(doctorId);
            logger.debug("Đã tìm thấy {} bệnh nhân của bác sĩ {}", patients.size(), doctorId);
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân của bác sĩ {}: {}", doctorId, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân của bác sĩ", e);
        }
    }

    public List<Patient> getPatientsByTreatmentStatus(Patient.TreatmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái điều trị không được để trống");
        }
        logger.debug("Đang lấy danh sách bệnh nhân theo trạng thái điều trị: {}", status);
        try {
            List<Patient> patients = patientRepository.findByTreatmentStatus(status);
            logger.debug("Đã tìm thấy {} bệnh nhân với trạng thái điều trị {}", patients.size(), status);
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân theo trạng thái điều trị: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân theo trạng thái điều trị", e);
        }
    }

    @Transactional
    public Patient updateTreatmentStatus(Long id, Patient.TreatmentStatus status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("ID và trạng thái điều trị không được để trống");
        }
        logger.debug("Đang cập nhật trạng thái điều trị cho bệnh nhân ID: {}", id);
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

            // Validate status transition
            if (patient.getTreatmentStatus() == Patient.TreatmentStatus.DECEASED 
                && status != Patient.TreatmentStatus.DECEASED) {
                throw new IllegalStateException("Không thể thay đổi trạng thái của bệnh nhân đã mất");
            }

            // Update treatment status
            patient.setTreatmentStatus(status);

            // Update patient status based on treatment status
            updatePatientStatusBasedOnTreatment(patient, status);

            Patient updatedPatient = patientRepository.save(patient);
            logger.debug("Đã cập nhật trạng thái điều trị thành công cho bệnh nhân ID: {}", id);
            return updatedPatient;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái điều trị cho bệnh nhân ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật trạng thái điều trị", e);
        }
    }

    private void updatePatientStatusBasedOnTreatment(Patient patient, Patient.TreatmentStatus treatmentStatus) {
        switch (treatmentStatus) {
            case DECEASED:
                patient.setStatus(Patient.Status.DECEASED);
                break;
            case TRANSFERRED:
                patient.setStatus(Patient.Status.TRANSFERRED);
                break;
            case ACTIVE:
                patient.setStatus(Patient.Status.ACTIVE);
                break;
            case PAUSED:
            case COMPLETED:
            case DISCONTINUED:
                // Keep current status for other treatment statuses
                break;
        }
    }

    @Transactional
    public Patient updatePatientProfile(Long userId, Patient patientDetails) {
        if (userId == null || patientDetails == null) {
            throw new IllegalArgumentException("ID người dùng và thông tin bệnh nhân không được để trống");
        }
        logger.debug("Đang cập nhật hồ sơ bệnh nhân cho người dùng ID: {}", userId);
        try {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ bệnh nhân cho người dùng: " + userId));

            // Update patient specific fields
            if (patientDetails.getEmergencyContact() != null) {
                patient.setEmergencyContact(patientDetails.getEmergencyContact());
            }
            if (patientDetails.getEmergencyPhone() != null) {
                patient.setEmergencyPhone(patientDetails.getEmergencyPhone());
            }
            if (patientDetails.getInsuranceNumber() != null) {
                patient.setInsuranceNumber(patientDetails.getInsuranceNumber());
            }
            if (patientDetails.getNotes() != null) {
                patient.setNotes(patientDetails.getNotes());
            }

            // Update user fields if provided
            User user = patient.getUser();
            if (patientDetails.getUser() != null) {
                updateUserDetails(user, patientDetails.getUser());
            }

            Patient updatedPatient = patientRepository.save(patient);
            logger.debug("Đã cập nhật hồ sơ bệnh nhân thành công cho người dùng ID: {}", userId);
            return updatedPatient;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hồ sơ bệnh nhân cho người dùng ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật hồ sơ bệnh nhân", e);
        }
    }

    private void updateUserDetails(User user, User updatedDetails) {
        if (updatedDetails.getFullName() != null) {
            user.setFullName(updatedDetails.getFullName());
        }
        if (updatedDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(updatedDetails.getPhoneNumber());
        }
        if (updatedDetails.getEmail() != null) {
            user.setEmail(updatedDetails.getEmail());
        }
        if (updatedDetails.getAddress() != null) {
            user.setAddress(updatedDetails.getAddress());
        }
    }

    @Transactional
    public Patient startHivTreatment(Long id, LocalDate treatmentStartDate) {
        if (id == null || treatmentStartDate == null) {
            throw new IllegalArgumentException("ID bệnh nhân và ngày bắt đầu điều trị không được để trống");
        }
        logger.debug("Đang bắt đầu điều trị HIV cho bệnh nhân ID: {}", id);
        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

            patient.setTreatmentStartDate(treatmentStartDate);
            patient.setTreatmentStatus(Patient.TreatmentStatus.ACTIVE);
            patient.setStatus(Patient.Status.ACTIVE);

            Patient updatedPatient = patientRepository.save(patient);
            logger.debug("Đã bắt đầu điều trị HIV thành công cho bệnh nhân ID: {}", id);
            return updatedPatient;
        } catch (Exception e) {
            logger.error("Lỗi khi bắt đầu điều trị HIV cho bệnh nhân ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Không thể bắt đầu điều trị HIV", e);
        }
    }

    public long countPatientsByTreatmentStatus(Patient.TreatmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái điều trị không được để trống");
        }
        logger.debug("Đang đếm số bệnh nhân theo trạng thái điều trị: {}", status);
        try {
            long count = patientRepository.countByTreatmentStatus(status);
            logger.debug("Đã tìm thấy {} bệnh nhân với trạng thái điều trị {}", count, status);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số bệnh nhân theo trạng thái điều trị: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đếm số bệnh nhân theo trạng thái điều trị", e);
        }
    }

    public long countNewPatientsInLast7Days() {
        logger.debug("Đang đếm số bệnh nhân mới trong 7 ngày qua");
        try {
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            long count = patientRepository.countByCreatedAtAfter(sevenDaysAgo.atStartOfDay());
            logger.debug("Đã tìm thấy {} bệnh nhân mới trong 7 ngày qua", count);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số bệnh nhân mới: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đếm số bệnh nhân mới", e);
        }
    }

    public Page<Patient> getPatientsByDoctorPaged(Long doctorId, Pageable pageable) {
        if (doctorId == null || pageable == null) {
            throw new IllegalArgumentException("ID bác sĩ và thông tin phân trang không được để trống");
        }
        logger.debug("Đang lấy danh sách bệnh nhân của bác sĩ {} theo trang", doctorId);
        try {
            Page<Patient> patients = patientRepository.findByDoctorId(doctorId, pageable);
            logger.debug("Đã tìm thấy {} bệnh nhân trong trang hiện tại", patients.getNumberOfElements());
            return patients;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bệnh nhân của bác sĩ {} theo trang: {}", doctorId, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bệnh nhân của bác sĩ theo trang", e);
        }
    }

    public long countPatientsByDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("ID bác sĩ không được để trống");
        }
        logger.debug("Đang đếm số bệnh nhân của bác sĩ với ID: {}", doctorId);
        try {
            long count = patientRepository.countByDoctorId(doctorId);
            logger.debug("Số bệnh nhân của bác sĩ {}: {}", doctorId, count);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số bệnh nhân của bác sĩ {}: {}", doctorId, e.getMessage(), e);
            throw new RuntimeException("Không thể đếm số bệnh nhân của bác sĩ", e);
        }
    }
}

