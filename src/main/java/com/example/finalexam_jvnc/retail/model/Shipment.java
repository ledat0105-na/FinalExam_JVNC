package com.example.finalexam_jvnc.retail.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    @ManyToOne
    @JoinColumn(name = "OrderId", nullable = false)
    private SalesOrder order;

    private String trackingNumber;
    private String carrier;
    private String status; // PENDING, SHIPPED, IN_TRANSIT, DELIVERED, FAILED
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    
    @Lob
    private String notes;
}

