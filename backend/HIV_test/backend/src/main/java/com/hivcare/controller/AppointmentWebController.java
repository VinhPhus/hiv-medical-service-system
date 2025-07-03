package com.hivcare.controller;

import com.hivcare.entity.Appointment;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.service.AppointmentService;
import com.hivcare.service.DoctorService;
import com.hivcare.service.PatientService;
import com.hivcare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.security.Principal;
import org.springframework.data.domain.PageImpl;
import java.util.Optional;

@Controller
@RequestMapping("/appointment")
public class AppointmentWebController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentWebController.class);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR', 'PATIENT')")
    public String listAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            Principal principal) {
        try {
            User currentUser = userService.findByUsername(principal.getName());
            if (currentUser.getRole() == User.Role.PATIENT) {
                Optional<Patient> patientOpt = patientService.getPatientByUserId(currentUser.getId());
                if (patientOpt.isPresent()) {
                    Patient patient = patientOpt.get();
                    List<Appointment> patientAppointments = appointmentService.getAppointmentsByPatient(patient.getId());
                    Page<Appointment> appointments = new PageImpl<>(patientAppointments);
                    model.addAttribute("appointments", appointments);
                    model.addAttribute("currentPage", 0);
                    model.addAttribute("totalPages", 1);
                    model.addAttribute("totalItems", patientAppointments.size());
                } else {
                    model.addAttribute("appointments", new PageImpl<>(new java.util.ArrayList<>()));
                    model.addAttribute("currentPage", 0);
                    model.addAttribute("totalPages", 1);
                    model.addAttribute("totalItems", 0);
                }
            } else {
                Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Appointment> appointments = appointmentService.getAllAppointments(pageable);
                model.addAttribute("appointments", appointments);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", appointments.getTotalPages());
                model.addAttribute("totalItems", appointments.getTotalElements());
            }
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            return "appointment/list";
        } catch (Exception e) {
            logger.error("Error listing appointments", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách lịch hẹn");
            model.addAttribute("appointments", new org.springframework.data.domain.PageImpl<>(new java.util.ArrayList<>())) ;
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", 0);
            return "appointment/list";
        }
    }

    @GetMapping("/form")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public String showAppointmentForm(Model model) {
        try {
            logger.info("Showing appointment form");
            
            // Create new appointment with default status
            Appointment appointment = new Appointment();
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

            // Get active doctors and patients
            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            List<Patient> patients = patientService.getAllActivePatients();

            logger.debug("Found {} doctors and {} patients", doctors.size(), patients.size());

            // Add to model
            model.addAttribute("appointment", appointment);
            model.addAttribute("doctors", doctors);
            model.addAttribute("patients", patients);

            return "appointment/form";
        } catch (Exception e) {
            logger.error("Error showing appointment form", e);
            model.addAttribute("error", "Có lỗi xảy ra khi hiển thị form: " + e.getMessage());
            return "error/500";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public String saveAppointment(
            @ModelAttribute Appointment appointment,
            @RequestParam("appointmentDateOnly") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate appointmentDate,
            @RequestParam("appointmentTimeOnly") @DateTimeFormat(pattern = "HH:mm") LocalTime appointmentTime,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            logger.info("Saving appointment with date: {} and time: {}", appointmentDate, appointmentTime);
            appointment.setAppointmentDate(LocalDateTime.of(appointmentDate, appointmentTime));
            Appointment savedAppointment = appointmentService.saveAppointment(appointment);
            logger.info("Successfully saved appointment with ID: {}", savedAppointment.getId());
            redirectAttributes.addFlashAttribute("success", "Lịch hẹn đã được lưu thành công");
            return "redirect:/appointment/list";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error while saving appointment: {}", e.getMessage());
            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            List<Patient> patients = patientService.getAllActivePatients();
            model.addAttribute("doctors", doctors);
            model.addAttribute("patients", patients);
            model.addAttribute("error", e.getMessage());
            return "appointment/form";
        } catch (Exception e) {
            logger.error("Error saving appointment", e);
            model.addAttribute("error", "Có lỗi xảy ra khi lưu lịch hẹn: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            logger.info("Showing edit form for appointment ID: {}", id);

            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn với ID: " + id));

            List<Doctor> doctors = doctorService.getAllActiveDoctors();
            List<Patient> patients = patientService.getAllActivePatients();

            model.addAttribute("appointment", appointment);
            model.addAttribute("doctors", doctors);
            model.addAttribute("patients", patients);

            return "appointment/form";
        } catch (IllegalArgumentException e) {
            logger.warn("Appointment not found: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/appointment/list";
        } catch (Exception e) {
            logger.error("Error showing edit form", e);
            model.addAttribute("error", "Có lỗi xảy ra khi hiển thị form chỉnh sửa");
            return "error/500";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting appointment ID: {}", id);
            
            appointmentService.deleteAppointment(id);
            logger.info("Successfully deleted appointment ID: {}", id);
            
            redirectAttributes.addFlashAttribute("success", "Lịch hẹn đã được xóa thành công");
        } catch (IllegalArgumentException e) {
            logger.warn("Error deleting appointment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting appointment", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa lịch hẹn");
        }
        return "redirect:/appointment/list";
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'DOCTOR')")
    public String showAppointmentDetail(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + id));

        model.addAttribute("appointment", appointment);
        return "appointment/detail";
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public String cancelAppointment(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy lịch hẹn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy lịch hẹn: " + e.getMessage());
        }
        return "redirect:/appointment/list";
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'MANAGER', 'ADMIN')")
    public String getTodayAppointments(Model model) {
        List<Appointment> todayAppointments = appointmentService.getTodayAppointments();
        model.addAttribute("appointments", todayAppointments);
        return "appointment/list";
    }
} 