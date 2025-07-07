
package com.hivcare.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivcare.dto.request.AppointmentRequest;
import com.hivcare.entity.Appointment;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.repository.AppointmentRepository;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.PatientRepository;

@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Page<Appointment> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Optional<Appointment> getAppointmentByCode(String code) {
        return appointmentRepository.findByAppointmentCode(code);
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public List<Appointment> getDoctorAppointmentsByDate(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
    }

    public List<Appointment> getTodayAppointmentsByDoctor(Long doctorId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return appointmentRepository.findByDoctorIdAndDateBetween(doctorId, startOfDay, endOfDay);
    }

    public long countPendingAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.countByDoctorIdAndStatus(doctorId, Appointment.AppointmentStatus.SCHEDULED);
    }

    public long countPendingAppointments() {
        return appointmentRepository.countByStatus(Appointment.AppointmentStatus.SCHEDULED);
    }

    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByPatientIdAndDateAfterOrderByDateAsc(patientId, now);
    }

    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        logger.info("Saving appointment: {}", appointment);

        // Validate patient
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn bệnh nhân");
        }
        Patient patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân"));
        appointment.setPatient(patient);

        // Xác nhận bác sĩ
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
        appointment.setDoctor(doctor);

        // Xác thực ngày hẹn
        if (appointment.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày và giờ hẹn");
        }

        LocalDateTime now = LocalDateTime.now();
        if (appointment.getAppointmentDate().isBefore(now)) {
            throw new IllegalArgumentException("Thời gian hẹn không thể trong quá khứ");
        }

        // Xác thực giờ làm việc (8:00 - 17:00)
        LocalTime appointmentTime = appointment.getAppointmentDate().toLocalTime();
        if (appointmentTime.isBefore(LocalTime.of(8, 0)) || appointmentTime.isAfter(LocalTime.of(17, 0))) {
            throw new IllegalArgumentException("Giờ hẹn phải trong khoảng 8:00 - 17:00");
        }

        // Kiểm tra xem có xung đột không
        List<Appointment> doctorAppointments = getDoctorAppointmentsByDate(
            doctor.getId(),
            appointment.getAppointmentDate().minusHours(1),
            appointment.getAppointmentDate().plusHours(1)
        );

        for (Appointment existingAppointment : doctorAppointments) {
            if (!existingAppointment.getId().equals(appointment.getId()) && 
                !existingAppointment.getStatus().equals(Appointment.AppointmentStatus.CANCELLED)) {
                throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn khác trong khoảng thời gian này");
            }
        }

        // Đặt trạng thái mặc định cho các cuộc hẹn mới
        if (appointment.getId() == null) {
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        }

        // Lưu cuộc hẹn

        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Successfully saved appointment with ID: {}", savedAppointment.getId());

        return savedAppointment;
    }

    @Transactional
    public void deleteAppointment(Long id) {
        logger.info("Deleting appointment with ID: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn với ID: " + id));

        // Chỉ cho phép xóa các cuộc hẹn trong tương lai
        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể xóa lịch hẹn đã qua");
        }

        appointmentRepository.deleteById(id);
        logger.info("Successfully deleted appointment with ID: {}", id);
    }

    public List<Appointment> getUpcomingAppointmentsForReminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24); // 24 hours ahead
        return appointmentRepository.findUpcomingAppointmentsForReminder(now, reminderTime);
    }

    public boolean isDoctorAvailable(Long doctorId, LocalDateTime appointmentDate) {
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorAndDate(doctorId, appointmentDate);
        
        // Kiểm tra xem có xung đột không (khoảng đệm 60 phút)
        return existingAppointments.stream()
                .noneMatch(apt -> !apt.getStatus().equals(Appointment.AppointmentStatus.CANCELLED) &&
                        Math.abs(apt.getAppointmentDate().compareTo(appointmentDate)) < 60);
    }

    public long getTotalAppointments() {
        return appointmentRepository.count();
    }

    public long getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    public List<Appointment> getTodayAppointments() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return appointmentRepository.findByDateBetweenWithDetails(startOfDay, endOfDay);
    }

    public List<Appointment> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByDateAfterOrderByDateAsc(now);
    }

    public List<Appointment> getCompletedAppointments() {
        return appointmentRepository.findByStatusWithDetails(Appointment.AppointmentStatus.COMPLETED);
    }

    @Transactional
    public Appointment bookAppointment(Long patientId, AppointmentRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Tạo cuộc hẹn mới
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setReason(request.getReason());
        appointment.setAnonymous(request.isAnonymous());
        appointment.setOnline(request.isOnline());
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        // Xác thực và lưu
        return saveAppointment(appointment);
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Update fields
        appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        appointment.setAppointmentType(appointmentDetails.getAppointmentType());
        appointment.setReason(appointmentDetails.getReason());
        appointment.setNotes(appointmentDetails.getNotes());
        appointment.setAnonymous(appointmentDetails.isAnonymous());
        appointment.setOnline(appointmentDetails.isOnline());
        appointment.setMeetingLink(appointmentDetails.getMeetingLink());
        
        if (appointmentDetails.getDoctor() != null) {
            Doctor doctor = doctorRepository.findById(appointmentDetails.getDoctor().getId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            appointment.setDoctor(doctor);
        }

        // Xác thực và lưu
        return saveAppointment(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Chỉ cho phép hủy các cuộc hẹn trong tương lai
        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel past appointments");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setReason(reason);

        Appointment cancelledAppointment = appointmentRepository.save(appointment);

        return cancelledAppointment;
    }

    public List<Appointment> getRecentAppointments(int limit) {
        return appointmentRepository.findTop10ByOrderByAppointmentDateDesc();
    }
}
