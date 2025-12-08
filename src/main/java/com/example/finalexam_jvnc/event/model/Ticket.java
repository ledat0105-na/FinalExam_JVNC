package com.example.finalexam_jvnc.event.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "CategoryId")
    private Category category;

    @Column(nullable = false, unique = true, length = 50)
    private String ticketCode;

    @Column(nullable = false, length = 200)
    private String ticketName;

    @Column(nullable = false)
    private Double ticketPrice;

    @Column(length = 50)
    private String ticketType; // VIP, STANDARD, EARLY_BIRD

    @Lob
    private String description;
    
    private LocalDateTime eventDate;
    private String venue; // Địa điểm
    private Integer totalQuantity; // Tổng số vé
    private Integer availableQuantity; // Số vé còn lại

    private Boolean isActive = true;
}

