package com.example.finalexam_jvnc.iot.model;

import com.example.finalexam_jvnc.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "WorkTickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private Account customer;

    @ManyToOne
    @JoinColumn(name = "PackageId", nullable = false)
    private MaintenancePackage maintenancePackage;

    private String ticketNumber;
    private String status; // OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED
    
    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;
    
    private Double packagePrice;
    private Double additionalCost;
    private Double totalAmount;
    
    @Lob
    private String description;
    @Lob
    private String workDetails; // Chi tiết công việc (JSON)
    @Lob
    private String technician; // Kỹ thuật viên
}

