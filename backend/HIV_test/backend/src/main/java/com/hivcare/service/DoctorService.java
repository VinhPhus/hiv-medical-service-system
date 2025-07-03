package com.hivcare.service;

import com.hivcare.entity.Doctor;
import com.hivcare.entity.DoctorSchedule;
import com.hivcare.entity.User;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.DoctorScheduleRepository;
import com.hivcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        logger.debug("Đang lấy danh sách tất cả bác sĩ");
        try {
            List<Doctor> doctors = doctorRepository.findAllWithDetails();
            logger.debug("Đã lấy được {} bác sĩ", doctors.size());
            // Trigger lazy loading of user data
            doctors.forEach(doctor -> {
                if (doctor.getUser() != null) {
                    doctor.getUser().getFullName(); // Force loading of user data
                }
            });
            return doctors;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bác sĩ: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bác sĩ", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllActiveDoctors() {
        logger.debug("Đang lấy danh sách bác sĩ đang hoạt động");
        try {
            List<Doctor> doctors = doctorRepository.findByStatus(Doctor.Status.ACTIVE);
            logger.debug("Đã lấy được {} bác sĩ đang hoạt động", doctors.size());
            // Trigger lazy loading of user data
            doctors.forEach(doctor -> {
                if (doctor.getUser() != null) {
                    doctor.getUser().getFullName(); // Force loading of user data
                }
            });
            return doctors;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bác sĩ đang hoạt động: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách bác sĩ đang hoạt động", e);
        }
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        logger.debug("Đang lấy danh sách bác sĩ theo trang {}", pageable);
        try {
            Page<Doctor> doctors = doctorRepository.findAll(pageable);
            logger.debug("Đã lấy được {} bác sĩ trong trang", doctors.getNumberOfElements());
            return doctors;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách bác sĩ theo trang", e);
            throw e;
        }
    }

    public Optional<Doctor> getDoctorById(Long id) {
        logger.debug("Đang tìm bác sĩ với ID: {}", id);
        try {
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isPresent()) {
                logger.debug("Đã tìm thấy bác sĩ với ID: {}", id);
            } else {
                logger.debug("Không tìm thấy bác sĩ với ID: {}", id);
            }
            return doctor;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bác sĩ với ID: {}", id, e);
            throw e;
        }
    }

    public Optional<Doctor> getDoctorByCode(String code) {
        return doctorRepository.findByDoctorCode(code);
    }

    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    public Optional<Doctor> getDoctorByUsername(String username) {
        logger.debug("Đang tìm bác sĩ với username: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return doctorRepository.findByUserId(user.get().getId());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bác sĩ với username: {}", username, e);
            throw e;
        }
    }

    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findByAvailable(true);
    }

    public Page<Doctor> searchDoctors(String searchTerm, Pageable pageable) {
        return doctorRepository.findBySearchTerm(searchTerm, pageable);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationAndAvailable(specialization);
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialty(specialty);
    }

    public List<Doctor> getDoctorsByStatus(Doctor.Status status) {
        return doctorRepository.findByStatus(status);
    }

    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        logger.debug("Đang cập nhật thông tin bác sĩ với ID: {}", id);
        try {
            Doctor doctor = doctorRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + id));

            // Validate doctor details
            if (doctorDetails.getLicenseNumber() == null || doctorDetails.getLicenseNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Số giấy phép hành nghề không được để trống");
            }

            if (doctorDetails.getSpecialty() == null || doctorDetails.getSpecialty().trim().isEmpty()) {
                throw new IllegalArgumentException("Chuyên khoa không được để trống");
            }

            // Update user details
            User user = doctor.getUser();
            if (user != null) {
                if (doctorDetails.getUser() != null) {
                    if (doctorDetails.getUser().getFullName() != null) {
                        user.setFullName(doctorDetails.getUser().getFullName());
                    }
                    if (doctorDetails.getUser().getPhoneNumber() != null) {
                        user.setPhoneNumber(doctorDetails.getUser().getPhoneNumber());
                    }
                    if (doctorDetails.getUser().getEmail() != null) {
                        user.setEmail(doctorDetails.getUser().getEmail());
                    }
                    if (doctorDetails.getUser().getAddress() != null) {
                        user.setAddress(doctorDetails.getUser().getAddress());
                    }
                }
                userRepository.save(user);
            }

            // Update doctor specific details
            doctor.setLicenseNumber(doctorDetails.getLicenseNumber());
            doctor.setSpecialty(doctorDetails.getSpecialty());
            doctor.setSpecialization(doctorDetails.getSpecialization());
            doctor.setQualification(doctorDetails.getQualification());
            doctor.setExperienceYears(doctorDetails.getExperienceYears());
            doctor.setConsultationFee(doctorDetails.getConsultationFee());
            doctor.setBio(doctorDetails.getBio());
            doctor.setWorkingHours(doctorDetails.getWorkingHours());
            doctor.setStatus(doctorDetails.getStatus());
            doctor.setNotes(doctorDetails.getNotes());

            Doctor updatedDoctor = doctorRepository.save(doctor);
            logger.debug("Đã cập nhật thông tin bác sĩ thành công với ID: {}", id);
            return updatedDoctor;
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật thông tin bác sĩ với ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public Doctor updateDoctorProfile(Long userId, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + userId));

        // Update doctor specific fields
        doctor.setLicenseNumber(doctorDetails.getLicenseNumber());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setQualification(doctorDetails.getQualification());
        doctor.setExperienceYears(doctorDetails.getExperienceYears());
        doctor.setConsultationFee(doctorDetails.getConsultationFee());
        doctor.setBio(doctorDetails.getBio());
        doctor.setWorkingHours(doctorDetails.getWorkingHours());

        // Update user fields
        User user = doctor.getUser();
        user.setFullName(doctorDetails.getUser().getFullName());
        user.setPhoneNumber(doctorDetails.getUser().getPhoneNumber());
        userRepository.save(user);

        return doctorRepository.save(doctor);
    }

    public Doctor setAvailability(Long id, boolean available) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setAvailable(available);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));
        
        // Delete associated user
        userRepository.delete(doctor.getUser());
        
        // Delete doctor
        doctorRepository.delete(doctor);
    }

    // Schedule Management
    public List<DoctorSchedule> getDoctorSchedules(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public List<DoctorSchedule> getAvailableSchedules(Long doctorId) {
        return doctorScheduleRepository.findAvailableSchedulesByDoctorId(doctorId);
    }

    public List<DoctorSchedule> getDoctorScheduleByDay(Long doctorId, DayOfWeek dayOfWeek) {
        return doctorScheduleRepository.findByDoctorIdAndDayOfWeekAndAvailable(doctorId, dayOfWeek);
    }

    public DoctorSchedule createSchedule(Long doctorId, DoctorSchedule schedule) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        schedule.setDoctor(doctor);
        return doctorScheduleRepository.save(schedule);
    }

    public DoctorSchedule updateSchedule(Long scheduleId, DoctorSchedule scheduleDetails) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setDayOfWeek(scheduleDetails.getDayOfWeek());
        schedule.setStartTime(scheduleDetails.getStartTime());
        schedule.setEndTime(scheduleDetails.getEndTime());
        schedule.setAvailable(scheduleDetails.isAvailable());
        schedule.setMaxAppointments(scheduleDetails.getMaxAppointments());

        return doctorScheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long scheduleId) {
        doctorScheduleRepository.deleteById(scheduleId);
    }

    public void createDefaultSchedule(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Create default Monday to Friday schedule (8 AM - 5 PM)
        DayOfWeek[] workingDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                  DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

        for (DayOfWeek day : workingDays) {
            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setDoctor(doctor);
            schedule.setDayOfWeek(day);
            schedule.setStartTime(LocalTime.of(8, 0));
            schedule.setEndTime(LocalTime.of(17, 0));
            schedule.setAvailable(true);
            schedule.setMaxAppointments(20);
            
            doctorScheduleRepository.save(schedule);
        }

        // Saturday morning
        DoctorSchedule saturdaySchedule = new DoctorSchedule();
        saturdaySchedule.setDoctor(doctor);
        saturdaySchedule.setDayOfWeek(DayOfWeek.SATURDAY);
        saturdaySchedule.setStartTime(LocalTime.of(8, 0));
        saturdaySchedule.setEndTime(LocalTime.of(12, 0));
        saturdaySchedule.setAvailable(true);
        saturdaySchedule.setMaxAppointments(10);
        
        doctorScheduleRepository.save(saturdaySchedule);
    }

    public long getTotalDoctors() {
        return doctorRepository.count();
    }

    public long getAvailableDoctorsCount() {
        return doctorRepository.countAvailableDoctors();
    }

    public boolean isDoctorAvailable(Long doctorId, DayOfWeek dayOfWeek, LocalTime time) {
        List<DoctorSchedule> schedules = doctorScheduleRepository
                .findByDoctorIdAndDayOfWeekAndAvailable(doctorId, dayOfWeek);
        
        return schedules.stream()
                .anyMatch(schedule -> 
                    !time.isBefore(schedule.getStartTime()) && 
                    !time.isAfter(schedule.getEndTime()));
    }

    public List<Doctor> findAvailableDoctors() {
        logger.debug("Đang tìm danh sách bác sĩ có thể tiếp nhận bệnh nhân");
        try {
            List<Doctor> doctors = doctorRepository.findByAvailable(true);
            logger.debug("Đã tìm thấy {} bác sĩ có thể tiếp nhận bệnh nhân", doctors.size());
            return doctors;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm bác sĩ có thể tiếp nhận bệnh nhân: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tìm bác sĩ có thể tiếp nhận bệnh nhân", e);
        }
    }

    public long countDoctorsByStatus(Doctor.Status status) {
        return doctorRepository.countByStatus(status);
    }

    public List<DoctorSchedule> getDoctorSchedule(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId);
    }

    public DoctorSchedule addDoctorSchedule(Long doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        return doctorScheduleRepository.save(schedule);
    }

    public void removeDoctorSchedule(Long scheduleId) {
        doctorScheduleRepository.deleteById(scheduleId);
    }

    public void save(Doctor doctor) {
        doctorRepository.save(doctor);
    }
}
