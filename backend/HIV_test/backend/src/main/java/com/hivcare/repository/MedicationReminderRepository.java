package com.hivcare.repository;

import com.hivcare.entity.MedicationReminder;
import com.hivcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {
    
    List<MedicationReminder> findByPatientAndActiveTrue(Patient patient);
    
    List<MedicationReminder> findByPatientIdAndActiveTrue(Long patientId);
    
    @Query("SELECT mr FROM MedicationReminder mr WHERE mr.patient.id = :patientId AND mr.active = true ORDER BY mr.reminderTime ASC")
    List<MedicationReminder> findActiveRemindersByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT mr FROM MedicationReminder mr WHERE mr.reminderTime BETWEEN :startTime AND :endTime AND mr.active = true")
    List<MedicationReminder> findRemindersInTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT mr FROM MedicationReminder mr WHERE mr.patient.id = :patientId AND mr.medicationName LIKE %:medicationName% AND mr.active = true")
    List<MedicationReminder> findByPatientIdAndMedicationNameContaining(@Param("patientId") Long patientId, 
                                                                        @Param("medicationName") String medicationName);
    
    List<MedicationReminder> findByActiveTrueOrderByReminderTimeAsc();
}
