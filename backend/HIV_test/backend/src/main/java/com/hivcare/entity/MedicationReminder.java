package com.hivcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "medication_reminders")
public class MedicationReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(name = "medication_name")
    private String medicationName;

    @Column(name = "dosage")
    private String dosage;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "last_sent")
    private LocalDateTime lastSent;

    @Column(name = "next_reminder")
    private LocalDateTime nextReminder;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type")
    private ReminderType reminderType = ReminderType.DAILY;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors, getters and setters
    public MedicationReminder() {}

    public MedicationReminder(Treatment treatment, LocalTime reminderTime, String medicationName) {
        this.treatment = treatment;
        this.reminderTime = reminderTime;
        this.medicationName = medicationName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Treatment getTreatment() { return treatment; }
    public void setTreatment(Treatment treatment) { this.treatment = treatment; }

    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getLastSent() { return lastSent; }
    public void setLastSent(LocalDateTime lastSent) { this.lastSent = lastSent; }

    public LocalDateTime getNextReminder() { return nextReminder; }
    public void setNextReminder(LocalDateTime nextReminder) { this.nextReminder = nextReminder; }

    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum ReminderType {
        DAILY, WEEKLY, MONTHLY, CUSTOM
    }
}
