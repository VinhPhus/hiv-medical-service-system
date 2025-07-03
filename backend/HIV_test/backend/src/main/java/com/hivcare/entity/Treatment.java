package com.hivcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "treatments")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arv_protocol_id")
    private ArvProtocol arvProtocol;

    @Column(name = "treatment_code", unique = true)
    private String treatmentCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TreatmentStatus status = TreatmentStatus.ACTIVE;

    @Column(name = "dosage_instructions", columnDefinition = "TEXT")
    private String dosageInstructions;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "side_effects", columnDefinition = "TEXT")
    private String sideEffects;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MedicationReminder> medicationReminders = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (treatmentCode == null) {
            treatmentCode = generateTreatmentCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTreatmentCode() {
        return "TRT" + System.currentTimeMillis();
    }

    // Constructors
    public Treatment() {}

    public Treatment(Patient patient, Doctor doctor, ArvProtocol arvProtocol) {
        this.patient = patient;
        this.doctor = doctor;
        this.arvProtocol = arvProtocol;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public ArvProtocol getArvProtocol() { return arvProtocol; }
    public void setArvProtocol(ArvProtocol arvProtocol) { this.arvProtocol = arvProtocol; }

    public String getTreatmentCode() { return treatmentCode; }
    public void setTreatmentCode(String treatmentCode) { this.treatmentCode = treatmentCode; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public TreatmentStatus getStatus() { return status; }
    public void setStatus(TreatmentStatus status) { this.status = status; }

    public String getDosageInstructions() { return dosageInstructions; }
    public void setDosageInstructions(String dosageInstructions) { this.dosageInstructions = dosageInstructions; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSideEffects() { return sideEffects; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<MedicationReminder> getMedicationReminders() { return medicationReminders; }
    public void setMedicationReminders(Set<MedicationReminder> medicationReminders) { this.medicationReminders = medicationReminders; }

    public enum TreatmentStatus {
        ACTIVE, COMPLETED, PAUSED, DISCONTINUED
    }
}
