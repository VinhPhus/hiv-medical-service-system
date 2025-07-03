package com.hivcare.repository;

import com.hivcare.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    
    List<DoctorSchedule> findByDoctorId(Long doctorId);
    
    @Query("SELECT d FROM DoctorSchedule d WHERE d.doctor.id = :doctorId AND d.dayOfWeek = :dayOfWeek AND d.available = true")
    List<DoctorSchedule> findByDoctorIdAndDayOfWeekAndAvailable(
        @Param("doctorId") Long doctorId, 
        @Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    @Query("SELECT d FROM DoctorSchedule d WHERE d.doctor.id = :doctorId AND d.available = true ORDER BY d.dayOfWeek, d.startTime")
    List<DoctorSchedule> findAvailableSchedulesByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT d FROM DoctorSchedule d WHERE d.dayOfWeek = :dayOfWeek AND d.available = true")
    List<DoctorSchedule> findByDayOfWeekAndAvailable(@Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    void deleteByDoctorId(Long doctorId);
}
