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
public class ShipmentDTO {
    private Long shipmentId;
    private Long orderId;
    private String orderNumber;
    private String trackingNumber;
    private String carrier;
    private String status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}

