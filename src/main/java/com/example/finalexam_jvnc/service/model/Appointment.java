package com.example.finalexam_jvnc.service.model;

import com.example.finalexam_jvnc.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private Account customer;

    @ManyToOne
    @JoinColumn(name = "PackageId", nullable = false)
    private ServicePackage servicePackage;

    private String appointmentNumber;
    private String status; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private LocalDateTime appointmentDate;
    private LocalDateTime completedAt;
    
    private Double packagePrice;
    private Double additionalFee;
    private Double totalAmount;
    private Double paidAmount;
    
    @Lob
    private String notes;
    @Lob
    private String serviceProvider; // Người thực hiện dịch vụ
}

