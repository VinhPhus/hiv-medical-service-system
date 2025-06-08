package com.hivcare.repository;

import com.hivcare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    Optional<Doctor> findByUserId(Long userId);
    
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    
    @Query("SELECT d FROM Doctor d WHERE d.availableToday = true")
    List<Doctor> findAvailableToday();
    
    @Query("SELECT d FROM Doctor d WHERE d.rating >= :rating ORDER BY d.rating DESC")
    List<Doctor> findByRatingGreaterThanEqual(@Param("rating") Double rating);
    
    @Query("SELECT d FROM Doctor d JOIN d.user u WHERE u.fullName LIKE %:name%")
    List<Doctor> findByDoctorNameContaining(@Param("name") String name);
    
    @Query("SELECT d FROM Doctor d WHERE d.specialization LIKE %:specialization% AND d.availableToday = true")
    List<Doctor> findAvailableDoctorsBySpecialization(@Param("specialization") String specialization);
}
