package com.hivcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "arv_protocols")
public class ArvProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications; // JSON format: {"TDF": "300mg", "3TC": "300mg", "DTG": "50mg"}

    @Column(name = "dosage_per_day")
    private Integer dosagePerDay;

    @Column(name = "recommended_duration_weeks")
    private Integer recommendedDurationWeeks;

    @Enumerated(EnumType.STRING)
    @Column(name = "patient_category")
    private PatientCategory patientCategory;

    @Column(name = "contraindications", columnDefinition = "TEXT")
    private String contraindications;

    @Column(name = "side_effects", columnDefinition = "TEXT")
    private String sideEffects;

    @Column(name = "monitoring_requirements", columnDefinition = "TEXT")
    private String monitoringRequirements;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "arvProtocol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Treatment> treatments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ArvProtocol() {}

    public ArvProtocol(String name, String code, String description, PatientCategory patientCategory) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.patientCategory = patientCategory;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public Integer getDosagePerDay() { return dosagePerDay; }
    public void setDosagePerDay(Integer dosagePerDay) { this.dosagePerDay = dosagePerDay; }

    public Integer getRecommendedDurationWeeks() { return recommendedDurationWeeks; }
    public void setRecommendedDurationWeeks(Integer recommendedDurationWeeks) { this.recommendedDurationWeeks = recommendedDurationWeeks; }

    public PatientCategory getPatientCategory() { return patientCategory; }
    public void setPatientCategory(PatientCategory patientCategory) { this.patientCategory = patientCategory; }

    public String getContraindications() { return contraindications; }
    public void setContraindications(String contraindications) { this.contraindications = contraindications; }

    public String getSideEffects() { return sideEffects; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }

    public String getMonitoringRequirements() { return monitoringRequirements; }
    public void setMonitoringRequirements(String monitoringRequirements) { this.monitoringRequirements = monitoringRequirements; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Treatment> getTreatments() { return treatments; }
    public void setTreatments(Set<Treatment> treatments) { this.treatments = treatments; }

    public enum PatientCategory {
        ADULT, PREGNANT_WOMAN, CHILD, ADOLESCENT, ELDERLY
    }
}
