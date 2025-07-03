package com.hivcare.repository;

import com.hivcare.entity.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    
    List<MedicationReminder> findByTreatmentId(Long treatmentId);
    
    @Query("SELECT m FROM MedicationReminder m WHERE m.active = true AND m.nextReminder <= :currentTime")
    List<MedicationReminder> findDueReminders(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT m FROM MedicationReminder m WHERE m.treatment.patient.id = :patientId AND m.active = true")
    List<MedicationReminder> findActiveByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT m FROM MedicationReminder m WHERE m.treatment.patient.id = :patientId")
    List<MedicationReminder> findByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT m FROM MedicationReminder m WHERE m.active = true")
    List<MedicationReminder> findAllActive();
    
    @Query("SELECT COUNT(m) FROM MedicationReminder m WHERE m.active = true")
    long countActiveReminders();
}
