package com.hivcare.entity;

import com.hivcare.enums.TestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestType testType;
    
    @Column(name = "test_value")
    private String testValue;
    
    @Column(name = "reference_range")
    private String referenceRange;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "test_date", nullable = false)
    private LocalDateTime testDate;
    
    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;
    
    @Column(name = "is_normal")
    private Boolean isNormal;
    
    @Column(name = "trend") // UP, DOWN, STABLE
    private String trend;
    
    @Column(name = "previous_value")
    private String previousValue;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
