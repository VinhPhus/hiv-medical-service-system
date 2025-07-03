package com.hivcare.repository;

import com.hivcare.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentRepositoryFixed extends JpaRepository<Treatment, Long> {

    Optional<Treatment> findByTreatmentCode(String treatmentCode);
    
    List<Treatment> findByPatientId(Long patientId);
    
    List<Treatment> findByDoctorId(Long doctorId);
    
    @Query("SELECT t FROM Treatment t WHERE t.patient.id = :patientId AND t.active = true")
    List<Treatment> findActiveByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT t FROM Treatment t WHERE t.status = :status")
    List<Treatment> findByStatus(@Param("status") Treatment.TreatmentStatus status);
    
    @Query("SELECT t FROM Treatment t WHERE t.endDate < :currentDate AND t.status = 'ACTIVE'")
    List<Treatment> findExpiredTreatments(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT t FROM Treatment t WHERE t.arvProtocol.id = :protocolId")
    List<Treatment> findByArvProtocolId(@Param("protocolId") Long protocolId);
    
    @Query("SELECT COUNT(t) FROM Treatment t WHERE t.status = :status")
    long countByStatus(@Param("status") Treatment.TreatmentStatus status);
}
