package com.hivcare.repository;

import com.hivcare.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByDoctorCode(String doctorCode);
    
    Optional<Doctor> findByUserId(Long userId);
    
    List<Doctor> findByAvailable(boolean available);
    
    @Query("SELECT d FROM Doctor d WHERE d.user.fullName ILIKE %:searchTerm% OR d.specialization ILIKE %:searchTerm%")
    Page<Doctor> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT d FROM Doctor d WHERE d.specialization = :specialization AND d.available = true")
    List<Doctor> findBySpecializationAndAvailable(@Param("specialization") String specialization);
    
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.available = true")
    long countAvailableDoctors();

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.user WHERE d.status = :status")
    List<Doctor> findByStatus(@Param("status") Doctor.Status status);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.user")
    List<Doctor> findAllWithDetails();

    @Query("SELECT d FROM Doctor d WHERE d.specialization = :specialty")
    List<Doctor> findBySpecialty(@Param("specialty") String specialty);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.status = :status")
    long countByStatus(@Param("status") Doctor.Status status);
}
