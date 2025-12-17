package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDTO {
    private Long walletTransactionId;
    private String transactionType; // TOPUP, PAYMENT, REFUND
    private Double amount;
    private String description;
    private LocalDateTime createdAt;
    private String orderNumber; // If related to an order
}

