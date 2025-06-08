package com.hivcare.repository;

import com.hivcare.entity.TestResult;
import com.hivcare.enums.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    List<TestResult> findByPatientIdOrderByTestDateDesc(Long patientId);
    
    List<TestResult> findByPatientIdAndTestTypeOrderByTestDateDesc(Long patientId, TestType testType);
    
    @Query("SELECT tr FROM TestResult tr WHERE tr.patient.id = :patientId AND tr.testType = :testType ORDER BY tr.testDate DESC LIMIT 1")
    Optional<TestResult> findLatestTestResultByPatientAndType(@Param("patientId") Long patientId, 
                                                            @Param("testType") TestType testType);
    
    @Query("SELECT tr FROM TestResult tr WHERE tr.patient.id = :patientId AND tr.testDate BETWEEN :startDate AND :endDate ORDER BY tr.testDate DESC")
    List<TestResult> findByPatientIdAndDateRange(@Param("patientId") Long patientId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT tr FROM TestResult tr WHERE tr.testType = :testType AND tr.testDate >= :date")
    List<TestResult> findByTestTypeAndTestDateAfter(@Param("testType") TestType testType, 
                                                   @Param("date") LocalDateTime date);
    
    @Query("SELECT tr FROM TestResult tr WHERE tr.patient.id = :patientId AND tr.testType IN :testTypes ORDER BY tr.testDate DESC")
    List<TestResult> findByPatientIdAndTestTypesIn(@Param("patientId") Long patientId, 
                                                  @Param("testTypes") List<TestType> testTypes);
}
