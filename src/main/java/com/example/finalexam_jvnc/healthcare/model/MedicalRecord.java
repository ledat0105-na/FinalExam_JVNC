package com.example.finalexam_jvnc.healthcare.model;

import com.example.finalexam_jvnc.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MedicalRecords")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne
    @JoinColumn(name = "PatientId", nullable = false)
    private Account patient;

    @ManyToOne
    @JoinColumn(name = "PackageId", nullable = false)
    private MedicalPackage medicalPackage;

    private String recordNumber;
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private LocalDateTime appointmentDate;
    private LocalDateTime completedAt;
    
    private Double packagePrice;
    private Double additionalFee;
    private Double totalAmount;
    private Double paidAmount;
    
    @Lob
    private String diagnosis; // Chẩn đoán
    @Lob
    private String prescription; // Đơn thuốc
    @Lob
    private String notes;
}

