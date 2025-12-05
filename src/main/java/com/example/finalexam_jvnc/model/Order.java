package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private Account customer;

    private String orderNumber;
    private String status;
    private String shippingAddress;
    private String billingAddress;

    private Double subtotal = 0.0;
    private Double discountTotal = 0.0;
    private Double taxAmount = 0.0;
    private Double shippingFee = 0.0;
    private Double codFee = 0.0;
    private Double gatewayFee = 0.0;
    private Double grandTotal = 0.0;
    private Double amountDue = 0.0;

    private LocalDateTime createdAt;
}

