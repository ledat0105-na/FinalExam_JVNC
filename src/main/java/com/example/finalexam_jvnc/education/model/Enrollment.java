package com.example.finalexam_jvnc.education.model;

import com.example.finalexam_jvnc.model.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "StudentId", nullable = false)
    private Account student;

    @ManyToOne
    @JoinColumn(name = "CourseId", nullable = false)
    private Course course;

    private String enrollmentNumber;
    private String status; // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    
    private LocalDateTime enrollmentDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private Double tuitionFee;
    private Double discountAmount;
    private Double totalAmount;
    private Double paidAmount;
    
    @Lob
    private String notes;
}

