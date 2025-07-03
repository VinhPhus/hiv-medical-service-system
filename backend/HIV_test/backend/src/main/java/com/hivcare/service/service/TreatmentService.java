package com.hivcare.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.entity.Treatment;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.PatientRepository;
import com.hivcare.repository.TreatmentRepository;
import com.hivcare.service.MedicationReminderService;

@SuppressWarnings("unused")
@Service
@Transactional
public class TreatmentService {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private MedicationReminderService medicationReminderService;

    public List<Treatment> getAllTreatments() {
        return treatmentRepository.findAll();
    }

    public Page<Treatment> getAllTreatments(Pageable pageable) {
        return treatmentRepository.findAll(pageable);
    }

    public Optional<Treatment> getTreatmentById(Long id) {
        return treatmentRepository.findById(id);
    }

    public Optional<Treatment> getTreatmentByCode(String code) {
        return treatmentRepository.findByTreatmentCode(code);
    }

    public List<Treatment> getTreatmentsByPatient(Long patientId) {
        return treatmentRepository.findByPatientId(patientId);
    }

    public List<Treatment> getActiveTreatmentsByPatient(Long patientId) {
        return treatmentRepository.findActiveByPatientId(patientId);
    }

    public Optional<Treatment> getCurrentTreatmentByPatient(Long patientId) {
        return treatmentRepository.findFirstByPatientIdAndEndDateIsNullOrderByStartDateDesc(patientId);
    }

    public List<Treatment> getTreatmentsByDoctor(Long doctorId) {
        return treatmentRepository.findByDoctorId(doctorId);
    }

    public Treatment createTreatment(Treatment treatment) {
        Patient patient = patientRepository.findById(treatment.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(treatment.getDoctor().getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        treatment.setPatient(patient);
        treatment.setDoctor(doctor);
        treatment.setActive(true);
        treatment.setStatus(Treatment.TreatmentStatus.ACTIVE);

        if (treatment.getStartDate() == null) {
            treatment.setStartDate(LocalDate.now());
        }

        Treatment savedTreatment = treatmentRepository.save(treatment);

        // Create medication reminders for this treatment
        medicationReminderService.createRemindersForTreatment(savedTreatment);

        return savedTreatment;
    }

    public Treatment updateTreatment(Long id, Treatment treatment) {
        if (treatmentRepository.existsById(id)) {
            treatment.setId(id);
            return treatmentRepository.save(treatment);
        }
        throw new RuntimeException("Treatment not found with id: " + id);
    }

    public Treatment completeTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.setStatus(Treatment.TreatmentStatus.COMPLETED);
        treatment.setEndDate(LocalDate.now());
        treatment.setActive(false);

        // Deactivate medication reminders
        medicationReminderService.deactivateRemindersForTreatment(id);

        return treatmentRepository.save(treatment);
    }

    public Treatment pauseTreatment(Long id, String reason) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.setStatus(Treatment.TreatmentStatus.PAUSED);
        treatment.setNotes(treatment.getNotes() + "\nPaused: " + reason);

        // Temporarily deactivate medication reminders
        medicationReminderService.pauseRemindersForTreatment(id);

        return treatmentRepository.save(treatment);
    }

    public Treatment resumeTreatment(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.setStatus(Treatment.TreatmentStatus.ACTIVE);

        // Reactivate medication reminders
        medicationReminderService.resumeRemindersForTreatment(id);

        return treatmentRepository.save(treatment);
    }

    public void deleteTreatment(Long id) {
        treatmentRepository.deleteById(id);
    }

    public List<Treatment> getExpiredTreatments() {
        return treatmentRepository.findExpiredTreatments(LocalDate.now());
    }

    public Treatment customizeArvProtocol(Long treatmentId, String customInstructions) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new RuntimeException("Treatment not found"));

        treatment.setDosageInstructions(customInstructions);
        treatment.setNotes(treatment.getNotes() + "\nCustomized ARV protocol: " + customInstructions);

        return treatmentRepository.save(treatment);
    }

    public long getTotalActiveTreatments() {
        return treatmentRepository.findByStatus(Treatment.TreatmentStatus.ACTIVE).size();
    }
}
