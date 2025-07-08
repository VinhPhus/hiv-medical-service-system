package com.hivcare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivcare.entity.ArvProtocol;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.MedicalRecord;
import com.hivcare.entity.Patient;
import com.hivcare.repository.ArvProtocolRepository;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.MedicalRecordRepository;
import com.hivcare.repository.PatientRepository;

@Service
@Transactional
public class MedicalRecordService {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ArvProtocolRepository arvProtocolRepository;

    public List<MedicalRecord> getAllMedicalRecords() {
        logger.debug("Lấy tất cả hồ sơ y tế");
        try {
            List<MedicalRecord> records = medicalRecordRepository.findAll();
            logger.debug("Đã tìm thấy {} hồ sơ y tế", records.size());
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy tất cả hồ sơ y tế", e);
            throw new RuntimeException("Không thể lấy danh sách hồ sơ y tế", e);
        }
    }

    public Page<MedicalRecord> getAllMedicalRecords(String search, Pageable pageable) {
        logger.debug("Tìm kiếm hồ sơ y tế với từ khóa: {}", search);
        try {
            Page<MedicalRecord> records = medicalRecordRepository.findAllWithSearch(search, pageable);
            logger.debug("Đã tìm thấy {} hồ sơ y tế", records.getTotalElements());
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm hồ sơ y tế", e);
            throw new RuntimeException("Không thể tìm kiếm hồ sơ y tế", e);
        }
    }

    public Optional<MedicalRecord> getMedicalRecordById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        logger.debug("Tìm hồ sơ y tế theo ID: {}", id);
        try {
            Optional<MedicalRecord> record = medicalRecordRepository.findById(id);
            if (record.isPresent()) {
                logger.debug("Đã tìm thấy hồ sơ y tế với ID: {}", id);
            } else {
                logger.debug("Không tìm thấy hồ sơ y tế với ID: {}", id);
            }
            return record;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm hồ sơ y tế theo ID: {}", id, e);
            throw new RuntimeException("Không thể tìm hồ sơ y tế", e);
        }
    }

    public Optional<MedicalRecord> getMedicalRecordByNumber(String recordNumber) {
        if (recordNumber == null || recordNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hồ sơ không được để trống");
        }
        logger.debug("Tìm hồ sơ y tế theo mã: {}", recordNumber);
        try {
            Optional<MedicalRecord> record = medicalRecordRepository.findByRecordNumber(recordNumber);
            if (record.isPresent()) {
                logger.debug("Đã tìm thấy hồ sơ y tế với mã: {}", recordNumber);
            } else {
                logger.debug("Không tìm thấy hồ sơ y tế với mã: {}", recordNumber);
            }
            return record;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm hồ sơ y tế theo mã: {}", recordNumber, e);
            throw new RuntimeException("Không thể tìm hồ sơ y tế theo mã", e);
        }
    }

    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Lấy danh sách hồ sơ y tế của bệnh nhân: {}", patientId);
        try {
            List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế của bệnh nhân {}", records.size(), patientId);
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế của bệnh nhân: {}", patientId, e);
            throw new RuntimeException("Không thể lấy hồ sơ y tế của bệnh nhân", e);
        }
    }

    public List<MedicalRecord> getMedicalRecordsByDoctorId(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("ID bác sĩ không được để trống");
        }
        logger.debug("Lấy danh sách hồ sơ y tế của bác sĩ: {}", doctorId);
        try {
            List<MedicalRecord> records = medicalRecordRepository.findByDoctorId(doctorId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế của bác sĩ {}", records.size(), doctorId);
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế của bác sĩ: {}", doctorId, e);
            throw new RuntimeException("Không thể lấy hồ sơ y tế của bác sĩ", e);
        }
    }

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Thông tin hồ sơ không được để trống");
        }
        logger.debug("Tạo hồ sơ y tế mới");
        try {
            validateMedicalRecord(record);
            
            // Tạo mã hồ sơ nếu chưa có
            if (record.getRecordNumber() == null || record.getRecordNumber().trim().isEmpty()) {
                record.setRecordNumber(generateRecordNumber());
            }

            MedicalRecord savedRecord = medicalRecordRepository.save(record);
            logger.debug("Đã tạo hồ sơ y tế mới với ID: {}", savedRecord.getId());
            return savedRecord;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hồ sơ y tế mới", e);
            throw new RuntimeException("Không thể tạo hồ sơ y tế mới", e);
        }
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord recordDetails) {
        if (id == null || recordDetails == null) {
            throw new IllegalArgumentException("ID và thông tin hồ sơ không được để trống");
        }
        logger.debug("Cập nhật hồ sơ y tế với ID: {}", id);
        try {
            MedicalRecord existingRecord = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));

            validateMedicalRecord(recordDetails);

            // Cập nhật thông tin
            existingRecord.setPatient(recordDetails.getPatient());
            existingRecord.setDoctor(recordDetails.getDoctor());
            existingRecord.setArvProtocol(recordDetails.getArvProtocol());
            existingRecord.setDiagnosis(recordDetails.getDiagnosis());
            existingRecord.setSymptoms(recordDetails.getSymptoms());
            existingRecord.setTreatment(recordDetails.getTreatment());
            existingRecord.setNotes(recordDetails.getNotes());
            existingRecord.setVisitDate(recordDetails.getVisitDate());
            existingRecord.setNextVisitDate(recordDetails.getNextVisitDate());

            MedicalRecord updatedRecord = medicalRecordRepository.save(existingRecord);
            logger.debug("Đã cập nhật hồ sơ y tế với ID: {}", id);
            return updatedRecord;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hồ sơ y tế với ID: {}", id, e);
            throw new RuntimeException("Không thể cập nhật hồ sơ y tế", e);
        }
    }

    @Transactional
    public void deleteMedicalRecord(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        logger.debug("Xóa hồ sơ y tế với ID: {}", id);
        try {
            if (!medicalRecordRepository.existsById(id)) {
                throw new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id);
            }
            medicalRecordRepository.deleteById(id);
            logger.debug("Đã xóa hồ sơ y tế với ID: {}", id);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa hồ sơ y tế với ID: {}", id, e);
            throw new RuntimeException("Không thể xóa hồ sơ y tế", e);
        }
    }

    private void validateMedicalRecord(MedicalRecord record) {
        // Kiểm tra bệnh nhân
        if (record.getPatient() == null || record.getPatient().getId() == null) {
            logger.error("Thiếu thông tin bệnh nhân khi lưu hồ sơ y tế");
            throw new IllegalArgumentException("Vui lòng chọn bệnh nhân");
        }
        Patient patient = patientRepository.findById(record.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân với ID: " + record.getPatient().getId()));
        record.setPatient(patient);

        // Kiểm tra bác sĩ
        if (record.getDoctor() == null || record.getDoctor().getId() == null) {
            logger.error("Thiếu thông tin bác sĩ khi lưu hồ sơ y tế");
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ phụ trách");
        }
        Doctor doctor = doctorRepository.findById(record.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + record.getDoctor().getId()));
        record.setDoctor(doctor);

        // Kiểm tra phác đồ ARV nếu có
        if (record.getArvProtocol() != null && record.getArvProtocol().getId() != null) {
            ArvProtocol protocol = arvProtocolRepository.findById(record.getArvProtocol().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phác đồ ARV với ID: " + record.getArvProtocol().getId()));
            record.setArvProtocol(protocol);
        }

        // Kiểm tra ngày
        if (record.getVisitDate() == null) {
            logger.error("Thiếu ngày khám khi lưu hồ sơ y tế");
            throw new IllegalArgumentException("Vui lòng nhập ngày khám");
        }
        if (record.getNextVisitDate() == null) {
            record.setNextVisitDate(record.getVisitDate().plusMonths(1));
        }
        if (record.getNextVisitDate().isBefore(record.getVisitDate())) {
            logger.error("Ngày tái khám trước ngày khám");
            throw new IllegalArgumentException("Ngày tái khám không thể trước ngày khám");
        }
    }

    private String generateRecordNumber() {
        return "MR" + System.currentTimeMillis();
    }

    public long countMedicalRecordsByPatient(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Đếm số hồ sơ y tế của bệnh nhân: {}", patientId);
        try {
            long count = medicalRecordRepository.countByPatientId(patientId);
            logger.debug("Số hồ sơ y tế của bệnh nhân {}: {}", patientId, count);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số hồ sơ y tế của bệnh nhân: {}", patientId, e);
            throw new RuntimeException("Không thể đếm số hồ sơ y tế của bệnh nhân", e);
        }
    }

    public long countMedicalRecordsByDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("ID bác sĩ không được để trống");
        }
        logger.debug("Đếm số hồ sơ y tế của bác sĩ: {}", doctorId);
        try {
            long count = medicalRecordRepository.countByDoctorId(doctorId);
            logger.debug("Số hồ sơ y tế của bác sĩ {}: {}", doctorId, count);
            return count;
        } catch (Exception e) {
            logger.error("Lỗi khi đếm số hồ sơ y tế của bác sĩ: {}", doctorId, e);
            throw new RuntimeException("Không thể đếm số hồ sơ y tế của bác sĩ", e);
        }
    }

    public List<MedicalRecord> getPatientMedicalHistory(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Lấy lịch sử khám bệnh của bệnh nhân: {}", patientId);
        try {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
            logger.debug("Đã tìm thấy {} hồ sơ y tế trong lịch sử khám bệnh của bệnh nhân {}", records.size(), patientId);
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy lịch sử khám bệnh của bệnh nhân: {}", patientId, e);
            throw new RuntimeException("Không thể lấy lịch sử khám bệnh của bệnh nhân", e);
        }
    }

    public Optional<MedicalRecord> getLatestMedicalRecordByPatient(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("ID bệnh nhân không được để trống");
        }
        logger.debug("Lấy hồ sơ y tế mới nhất của bệnh nhân: {}", patientId);
        try {
            Optional<MedicalRecord> record = medicalRecordRepository.findFirstByPatientIdOrderByVisitDateDesc(patientId);
            if (record.isPresent()) {
                logger.debug("Đã tìm thấy hồ sơ y tế mới nhất của bệnh nhân {}", patientId);
            } else {
                logger.debug("Không tìm thấy hồ sơ y tế nào của bệnh nhân {}", patientId);
            }
            return record;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế mới nhất của bệnh nhân: {}", patientId, e);
            throw new RuntimeException("Không thể lấy hồ sơ y tế mới nhất của bệnh nhân", e);
        }
    }

    public List<MedicalRecord> getMedicalRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc không thể trước ngày bắt đầu");
        }
        logger.debug("Lấy danh sách hồ sơ y tế từ {} đến {}", startDate, endDate);
        try {
            List<MedicalRecord> records = medicalRecordRepository.findByVisitDateBetweenOrderByVisitDateDesc(startDate, endDate);
            logger.debug("Đã tìm thấy {} hồ sơ y tế trong khoảng thời gian", records.size());
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách hồ sơ y tế theo khoảng thời gian", e);
            throw new RuntimeException("Không thể lấy danh sách hồ sơ y tế theo khoảng thời gian", e);
        }
    }

    @Transactional
    public MedicalRecord createFromAppointment(Long patientId, Long doctorId, String chiefComplaint) {
        if (patientId == null || doctorId == null) {
            throw new IllegalArgumentException("ID bệnh nhân và bác sĩ không được để trống");
        }
        logger.debug("Tạo hồ sơ y tế từ lịch hẹn cho bệnh nhân {} và bác sĩ {}", patientId, doctorId);
        try {
        Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
            record.setNotes(chiefComplaint);
        record.setVisitDate(LocalDateTime.now());
            record.setNextVisitDate(LocalDateTime.now().plusMonths(1));

            return createMedicalRecord(record);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hồ sơ y tế từ lịch hẹn", e);
            throw new RuntimeException("Không thể tạo hồ sơ y tế từ lịch hẹn", e);
        }
    }

    @Transactional
    public MedicalRecord addDiagnosis(Long id, String diagnosis, String treatmentPlan) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        logger.debug("Thêm chẩn đoán cho hồ sơ y tế với ID: {}", id);
        try {
        MedicalRecord record = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));

        record.setDiagnosis(diagnosis);
            record.setTreatment(treatmentPlan);
            
            MedicalRecord updatedRecord = medicalRecordRepository.save(record);
            logger.debug("Đã thêm chẩn đoán cho hồ sơ y tế với ID: {}", id);
            return updatedRecord;
        } catch (Exception e) {
            logger.error("Lỗi khi thêm chẩn đoán cho hồ sơ y tế với ID: {}", id, e);
            throw new RuntimeException("Không thể thêm chẩn đoán cho hồ sơ y tế", e);
        }
    }

    @Transactional
    public MedicalRecord addPrescription(Long id, String prescriptions) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        logger.debug("Thêm đơn thuốc cho hồ sơ y tế với ID: {}", id);
        try {
        MedicalRecord record = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));
            
            record.setTreatment(prescriptions);
            
            MedicalRecord updatedRecord = medicalRecordRepository.save(record);
            logger.debug("Đã thêm đơn thuốc cho hồ sơ y tế với ID: {}", id);
            return updatedRecord;
        } catch (Exception e) {
            logger.error("Lỗi khi thêm đơn thuốc cho hồ sơ y tế với ID: {}", id, e);
            throw new RuntimeException("Không thể thêm đơn thuốc cho hồ sơ y tế", e);
        }
    }

    @Transactional
    public MedicalRecord addFollowUpInstructions(Long id, String instructions) {
        if (id == null) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        logger.debug("Thêm hướng dẫn theo dõi cho hồ sơ y tế với ID: {}", id);
        try {
        MedicalRecord record = medicalRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ y tế với ID: " + id));
            
            record.setNotes(instructions);
            
            MedicalRecord updatedRecord = medicalRecordRepository.save(record);
            logger.debug("Đã thêm hướng dẫn theo dõi cho hồ sơ y tế với ID: {}", id);
            return updatedRecord;
        } catch (Exception e) {
            logger.error("Lỗi khi thêm hướng dẫn theo dõi cho hồ sơ y tế với ID: {}", id, e);
            throw new RuntimeException("Không thể thêm hướng dẫn theo dõi cho hồ sơ y tế", e);
        }
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getLatestRecordsForAllPatients() {
        logger.debug("Lấy hồ sơ y tế mới nhất cho tất cả bệnh nhân");
        try {
            List<MedicalRecord> records = medicalRecordRepository.findLatestRecordsForAllPatients();
            logger.debug("Đã tìm thấy {} hồ sơ y tế mới nhất", records.size());
            return records;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy hồ sơ y tế mới nhất cho tất cả bệnh nhân", e);
            throw new RuntimeException("Không thể lấy hồ sơ y tế mới nhất", e);
        }
    }
}
