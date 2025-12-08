package com.example.finalexam_jvnc.event.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CheckIns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkInId;

    @ManyToOne
    @JoinColumn(name = "OrderId", nullable = false)
    private TicketOrder ticketOrder;

    private String checkInNumber;
    private String status; // PENDING, CHECKED_IN, CANCELLED
    private LocalDateTime checkInTime;
    private String checkInLocation; // Vị trí check-in
    
    @Lob
    private String notes;
}

