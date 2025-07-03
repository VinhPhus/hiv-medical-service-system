package com.hivcare.repository;

import com.hivcare.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);
    
    List<MedicalRecord> findByPatientId(Long patientId);
    
    List<MedicalRecord> findByDoctorId(Long doctorId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE " +
           "(:search IS NULL OR " +
           "LOWER(m.recordNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.patient.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MedicalRecord> findAllWithSearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE m.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE m.doctor.id = :doctorId")
    long countByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId ORDER BY m.visitDate DESC")
    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(@Param("patientId") Long patientId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.doctor.id = :doctorId ORDER BY m.visitDate DESC")
    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(@Param("doctorId") Long doctorId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.visitDate BETWEEN :startDate AND :endDate")
    List<MedicalRecord> findByVisitDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId AND m.visitDate BETWEEN :startDate AND :endDate")
    List<MedicalRecord> findByPatientIdAndVisitDateBetween(
        @Param("patientId") Long patientId,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId ORDER BY m.visitDate DESC")
    Optional<MedicalRecord> findFirstByPatientIdOrderByVisitDateDesc(@Param("patientId") Long patientId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.visitDate BETWEEN :startDate AND :endDate ORDER BY m.visitDate DESC")
    List<MedicalRecord> findByVisitDateBetweenOrderByVisitDateDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT m FROM MedicalRecord m 
        WHERE m.id IN (
            SELECT MAX(m2.id) 
            FROM MedicalRecord m2 
            GROUP BY m2.patient.id
        )
        ORDER BY m.visitDate DESC""")
    List<MedicalRecord> findLatestRecordsForAllPatients();
}
