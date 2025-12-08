package com.example.finalexam_jvnc.education.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TuitionPayments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TuitionPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "EnrollmentId", nullable = false)
    private Enrollment enrollment;

    private String paymentNumber;
    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD
    private Double amount;
    private String status; // PENDING, PAID, REFUNDED
    private LocalDateTime paidAt;
    private String transactionRef;
    
    @Lob
    private String notes;
}

