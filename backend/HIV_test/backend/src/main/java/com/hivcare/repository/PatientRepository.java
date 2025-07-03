package com.hivcare.repository;

import com.hivcare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientCode(String patientCode);
    
    Optional<Patient> findByUserId(Long userId);
    
    @Query("SELECT p FROM Patient p WHERE p.user.fullName ILIKE %:searchTerm% OR p.patientCode ILIKE %:searchTerm%")
    Page<Patient> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE p.treatmentStatus = :status")
    List<Patient> findByTreatmentStatus(@Param("status") Patient.TreatmentStatus status);
    
    @Query("SELECT p FROM Patient p WHERE p.hivDiagnosisDate BETWEEN :startDate AND :endDate")
    List<Patient> findByHivDiagnosisDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.treatmentStatus = :status")
    long countByTreatmentStatus(@Param("status") Patient.TreatmentStatus status);
    
    @Query("SELECT p FROM Patient p WHERE p.doctor.id = :doctorId")
    List<Patient> findByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT p FROM Patient p WHERE p.status = :status")
    List<Patient> findByStatus(@Param("status") Patient.Status status);

    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.user")
    List<Patient> findAllWithDetails();

    @Query(value = "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.treatments",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Patient p")
    Page<Patient> findAllWithUser(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt >= :date")
    long countByCreatedAtAfter(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.user WHERE p.doctor.id = :doctorId")
    Page<Patient> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.doctor.id = :doctorId")
    long countByDoctorId(@Param("doctorId") Long doctorId);
}
