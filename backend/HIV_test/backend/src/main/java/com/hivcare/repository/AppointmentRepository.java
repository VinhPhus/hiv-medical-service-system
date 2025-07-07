
package com.hivcare.repository;

import com.hivcare.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentCode(String appointmentCode);
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorId(Long doctorId);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndDateBetween(@Param("doctorId") Long doctorId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndDateBetween(@Param("patientId") Long patientId, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.status = :status")
    List<Appointment> findByStatus(@Param("status") Appointment.AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate AND a.reminderSent = false")
    List<Appointment> findUpcomingAppointmentsForReminder(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = :status")
    long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") Appointment.AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE (:doctorId IS NULL OR a.doctor.id = :doctorId) AND DATE(a.appointmentDate) = DATE(:date)")
    List<Appointment> findByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDateTime date);

    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.status = :status")
    List<Appointment> findByStatusWithDetails(@Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDateBetweenWithDetails(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Appointment> findByAppointmentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndAppointmentDateBetween(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentType = :type")
    List<Appointment> findByType(@Param("type") Appointment.AppointmentType type);

    @Query("SELECT a FROM Appointment a WHERE " +
           "LOWER(a.patient.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.doctor.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Appointment> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor ORDER BY a.appointmentDate DESC")
    List<Appointment> findTop10ByOrderByAppointmentDateDesc();

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate > :date ORDER BY a.appointmentDate ASC")
    List<Appointment> findByPatientIdAndDateAfterOrderByDateAsc(@Param("patientId") Long patientId, @Param("date") LocalDateTime date);

    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor WHERE a.appointmentDate > :date ORDER BY a.appointmentDate ASC")
    List<Appointment> findByDateAfterOrderByDateAsc(@Param("date") LocalDateTime date);
}
