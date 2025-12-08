package com.example.finalexam_jvnc.healthcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MedicalFees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feeId;

    @ManyToOne
    @JoinColumn(name = "RecordId", nullable = false)
    private MedicalRecord medicalRecord;

    private String feeNumber;
    private String paymentMethod;
    private Double amount;
    private String status; // PENDING, PAID, REFUNDED
    private LocalDateTime paidAt;
    private String transactionRef;
    
    @Lob
    private String notes;
}

