package com.example.finalexam_jvnc.event.model;

import com.example.finalexam_jvnc.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TicketOrders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private Account customer;

    @ManyToOne
    @JoinColumn(name = "TicketId", nullable = false)
    private Ticket ticket;

    private String orderNumber;
    private String status; // PENDING, CONFIRMED, CANCELLED
    
    private Integer quantity;
    private Double unitPrice;
    private Double discountAmount;
    private Double totalAmount;
    private Double paidAmount;
    
    private LocalDateTime orderDate;
    private LocalDateTime eventDate;
    
    @Lob
    private String notes;
}

