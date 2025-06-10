package com.hivcare.repository;

import com.hivcare.entity.Doctor;
import com.hivcare.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    
    List<DoctorSchedule> findByDoctorAndAvailableTrue(Doctor doctor);
    
    List<DoctorSchedule> findByDoctorIdAndAvailableTrue(Long doctorId);
    
    List<DoctorSchedule> findByDayOfWeekAndAvailableTrue(DayOfWeek dayOfWeek);
    
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.dayOfWeek = :dayOfWeek AND ds.available = true")
    List<DoctorSchedule> findByDoctorIdAndDayOfWeek(@Param("doctorId") Long doctorId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.dayOfWeek = :dayOfWeek AND ds.startTime <= :time AND ds.endTime >= :time AND ds.available = true")
    Optional<DoctorSchedule> findByDoctorIdAndDayOfWeekAndTime(@Param("doctorId") Long doctorId, 
                                                              @Param("dayOfWeek") DayOfWeek dayOfWeek, 
                                                              @Param("time") LocalTime time);
    
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.dayOfWeek = :dayOfWeek AND ds.startTime <= :time AND ds.endTime >= :time AND ds.available = true")
    List<DoctorSchedule> findAvailableDoctorsAtTime(@Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("time") LocalTime time);
    
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.startTime >= :startTime AND ds.endTime <= :endTime AND ds.available = true")
    List<DoctorSchedule> findByDoctorIdAndTimeRange(@Param("doctorId") Long doctorId, 
                                                   @Param("startTime") LocalTime startTime, 
                                                   @Param("endTime") LocalTime endTime);
    
    boolean existsByDoctorIdAndDayOfWeekAndStartTimeAndEndTime(Long doctorId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
    
    @Query("SELECT COUNT(ds) FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId AND ds.available = true")
    Long countAvailableSlotsByDoctorId(@Param("doctorId") Long doctorId);
}
