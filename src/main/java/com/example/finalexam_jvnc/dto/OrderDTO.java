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
public class OrderDTO {
    private Long orderId;
    private Long customerId;
    private String customerUsername;
    private String orderNumber;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingFee;
    private Double codFee;
    private Double gatewayFee;
    private Double grandTotal;
    private Double amountDue;
    private LocalDateTime createdAt;
}

