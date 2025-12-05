package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    private String paymentMethod;
    private Double amount;
    private String status;
    private LocalDateTime paidAt;
    private String transactionRef;
}

