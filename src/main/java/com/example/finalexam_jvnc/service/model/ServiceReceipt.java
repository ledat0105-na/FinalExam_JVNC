package com.example.finalexam_jvnc.service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ServiceReceipts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    @ManyToOne
    @JoinColumn(name = "AppointmentId", nullable = false)
    private Appointment appointment;

    private String receiptNumber;
    private String paymentMethod;
    private Double amount;
    private String status; // PENDING, PAID, REFUNDED
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    
    @Lob
    private String notes;
}

