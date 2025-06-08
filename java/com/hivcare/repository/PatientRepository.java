package com.hivcare.repository;

import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);
    
    Optional<Patient> findByPatientCode(String patientCode);
    
    List<Patient> findByCurrentArvRegimen(String arvRegimen);
    
    @Query("SELECT p FROM Patient p WHERE p.adherenceRate < :threshold")
    List<Patient> findPatientsWithLowAdherence(@Param("threshold") Double threshold);
    
    @Query("SELECT p FROM Patient p WHERE p.latestCd4 < :cd4Threshold")
    List<Patient> findPatientsWithLowCD4(@Param("cd4Threshold") Integer cd4Threshold);
    
    @Query("SELECT p FROM Patient p WHERE p.diagnosisDate BETWEEN :startDate AND :endDate")
    List<Patient> findPatientsByDiagnosisDateRange(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Patient p")
    Long countTotalPatients();
    
    @Query("SELECT AVG(p.adherenceRate) FROM Patient p")
    Double getAverageAdherenceRate();
    
    @Query("SELECT p FROM Patient p WHERE p.updatedAt < :lastUpdateThreshold")
    List<Patient> findPatientsNeedingFollowUp(@Param("lastUpdateThreshold") LocalDateTime lastUpdateThreshold);
    
    @Query("SELECT p.currentArvRegimen, COUNT(p) FROM Patient p GROUP BY p.currentArvRegimen")
    List<Object[]> getARVRegimenStatistics();
}