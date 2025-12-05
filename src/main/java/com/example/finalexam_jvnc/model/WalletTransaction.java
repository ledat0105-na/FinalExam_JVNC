package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "WalletTransactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletTransactionId;

    @ManyToOne
    @JoinColumn(name = "WalletId", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "PaymentId")
    private Payment payment;

    private String transactionType; // TOPUP, PAYMENT, REFUND
    private Double amount;
    private String description;
    private LocalDateTime createdAt;
}

