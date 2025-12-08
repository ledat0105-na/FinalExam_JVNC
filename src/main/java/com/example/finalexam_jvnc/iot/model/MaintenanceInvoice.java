package com.example.finalexam_jvnc.iot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MaintenanceInvoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @ManyToOne
    @JoinColumn(name = "TicketId", nullable = false)
    private WorkTicket workTicket;

    private String invoiceNumber;
    private String paymentMethod;
    private Double amount;
    private String status; 
    private LocalDateTime issuedAt;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    
    @Lob
    private String notes;
}

