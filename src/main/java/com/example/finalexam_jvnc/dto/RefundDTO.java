package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDTO {
    private Long refundId;
    private Long orderId;
    private String orderNumber;
    private Long paymentId;
    private Double refundAmount;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
}

