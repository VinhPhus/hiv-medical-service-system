package com.hivcare.repository;

import com.hivcare.entity.TestResult;
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
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    Optional<TestResult> findByTestCode(String testCode);
    
    List<TestResult> findByPatientId(Long patientId);
    
    Page<TestResult> findByPatientId(Long patientId, Pageable pageable);
    
    List<TestResult> findByTestType(TestResult.TestType testType);
    
    @Query("SELECT t FROM TestResult t WHERE t.patient.id = :patientId AND t.testType = :testType ORDER BY t.testDate DESC")
    List<TestResult> findByPatientIdAndTestTypeOrderByTestDateDesc(
        @Param("patientId") Long patientId, 
        @Param("testType") TestResult.TestType testType);
    
    @Query("SELECT t FROM TestResult t WHERE t.status = :status")
    List<TestResult> findByStatus(@Param("status") TestResult.TestStatus status);
    
    @Query("SELECT t FROM TestResult t WHERE t.testDate BETWEEN :startDate AND :endDate")
    List<TestResult> findByTestDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM TestResult t WHERE t.patient.id = :patientId AND t.testDate BETWEEN :startDate AND :endDate")
    List<TestResult> findByPatientIdAndTestDateBetween(
        @Param("patientId") Long patientId,
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.status = :status")
    Long countByStatus(@Param("status") TestResult.TestStatus status);

    List<TestResult> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT t FROM TestResult t WHERE " +
           "(:search IS NULL OR " +
           "LOWER(t.testCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.patient.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.resultValue) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TestResult> findAllWithSearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM TestResult t WHERE t.patient.id = :patientId AND " +
           "(:search IS NULL OR " +
           "LOWER(t.testCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.resultValue) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TestResult> findByPatientIdWithSearch(
        @Param("patientId") Long patientId,
        @Param("search") String search,
        Pageable pageable);

    @Query("SELECT t FROM TestResult t WHERE t.patient.id = :patientId AND t.testType = :testType ORDER BY t.testDate DESC")
    List<TestResult> findLatestByPatientAndType(@Param("patientId") Long patientId, @Param("testType") TestResult.TestType testType);

    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.patient.id = :patientId AND t.status = 'COMPLETED'")
    long countCompletedTestsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.patient.id = :patientId AND t.status = 'PENDING'")
    long countPendingTestsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT t FROM TestResult t WHERE t.patient.doctor.id = :doctorId AND t.status = 'PENDING'")
    List<TestResult> findPendingTestsByDoctor(@Param("doctorId") Long doctorId);
}
