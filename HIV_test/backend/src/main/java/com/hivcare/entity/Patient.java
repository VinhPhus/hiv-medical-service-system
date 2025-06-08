package com.hivcare.entity;

import com.hivcare.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "patient_code", unique = true, nullable = false)
    private String patientCode;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Column(name = "diagnosis_date")
    private LocalDateTime diagnosisDate;
    
    @Column(name = "treatment_start_date")
    private LocalDateTime treatmentStartDate;
    
    @Column(name = "current_arv_regimen")
    private String currentArvRegimen;
    
    @Column(name = "adherence_rate")
    @Builder.Default
    private Double adherenceRate = 0.0;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestResult> testResults = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicationReminder> medicationReminders = new ArrayList<>();
    
    @Column(name = "next_appointment_date")
    private LocalDateTime nextAppointmentDate;
    
    @Column(name = "latest_cd4")
    private Integer latestCd4;
    
    @Column(name = "latest_viral_load")
    private String latestViralLoad;
    
    @Column(name = "treatment_notes", columnDefinition = "TEXT")
    private String treatmentNotes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}