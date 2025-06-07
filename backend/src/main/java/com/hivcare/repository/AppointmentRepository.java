package main.java.com.hivcare.repository;

import com.hivcare.entity.Appointment;
import com.hivcare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);
    
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :start AND :end AND a.status = :status")
    List<Appointment> findByAppointmentDateBetweenAndStatus(@Param("start") LocalDateTime start, 
                                                           @Param("end") LocalDateTime end, 
                                                           @Param("status") AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status IN :statuses ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId, 
                                                       @Param("statuses") List<AppointmentStatus> statuses);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND DATE(a.appointmentDate) = CURRENT_DATE ORDER BY a.appointmentDate ASC")
    List<Appointment> findTodayAppointmentsByDoctor(@Param("doctorId") Long doctorId);
    
    Optional<Appointment> findByAnonymousId(String anonymousId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = 'COMPLETED'")
    Long countCompletedAppointmentsByDoctor(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.reminderSent = false AND a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findAppointmentsNeedingReminder(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
