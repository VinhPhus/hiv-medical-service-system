package com.hivcare.controller;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hivcare.dto.response.ApiResponse;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.DoctorSchedule;
import com.hivcare.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Page<Doctor>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Doctor> doctors;
        if (search != null && !search.trim().isEmpty()) {
            doctors = doctorService.searchDoctors(search, pageable);
        } else {
            doctors = doctorService.getAllDoctors(pageable);
        }
        
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Doctor>> getAvailableDoctors() {
        List<Doctor> availableDoctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(availableDoctors);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('DOCTOR')")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok().body(doctor))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Doctor> getDoctorByCode(@PathVariable String code) {
        return doctorService.getDoctorByCode(code)
                .map(doctor -> ResponseEntity.ok().body(doctor))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Doctor> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return doctorService.getDoctorByUserId(getUserIdFromUsername(username))
                .map(doctor -> ResponseEntity.ok().body(doctor))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        try {
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDetails);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật hồ sơ bác sĩ: " + e.getMessage()));
        }
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateMyProfile(@RequestBody Doctor doctorDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Doctor updatedDoctor = doctorService.updateDoctorProfile(getUserIdFromUsername(username), doctorDetails);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật hồ sơ: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        try {
            Doctor updatedDoctor = doctorService.setAvailability(id, available);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa hồ sơ bác sĩ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa hồ sơ bác sĩ: " + e.getMessage()));
        }
    }

    // Schedule Management
    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<List<DoctorSchedule>> getDoctorSchedules(@PathVariable Long id) {
        List<DoctorSchedule> schedules = doctorService.getDoctorSchedules(id);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}/available-schedules")
    public ResponseEntity<List<DoctorSchedule>> getAvailableSchedules(@PathVariable Long id) {
        List<DoctorSchedule> schedules = doctorService.getAvailableSchedules(id);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}/schedules/{dayOfWeek}")
    public ResponseEntity<List<DoctorSchedule>> getDoctorScheduleByDay(
            @PathVariable Long id, 
            @PathVariable DayOfWeek dayOfWeek) {
        List<DoctorSchedule> schedules = doctorService.getDoctorScheduleByDay(id, dayOfWeek);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/{id}/schedules")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createSchedule(@PathVariable Long id, @RequestBody DoctorSchedule schedule) {
        try {
            DoctorSchedule savedSchedule = doctorService.createSchedule(id, schedule);
            return ResponseEntity.ok(savedSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo lịch làm việc: " + e.getMessage()));
        }
    }

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateSchedule(@PathVariable Long scheduleId, @RequestBody DoctorSchedule scheduleDetails) {
        try {
            DoctorSchedule updatedSchedule = doctorService.updateSchedule(scheduleId, scheduleDetails);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi cập nhật lịch làm việc: " + e.getMessage()));
        }
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            doctorService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(new ApiResponse(true, "Xóa lịch làm việc thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi xóa lịch làm việc: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/default-schedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createDefaultSchedule(@PathVariable Long id) {
        try {
            doctorService.createDefaultSchedule(id);
            return ResponseEntity.ok(new ApiResponse(true, "Tạo lịch làm việc mặc định thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lỗi tạo lịch làm việc mặc định: " + e.getMessage()));
        }
    }

    // Helper method to get user ID from username
    private Long getUserIdFromUsername(String username) {
        // This should be implemented properly based on your UserService
        return 1L; // Placeholder
    }
}