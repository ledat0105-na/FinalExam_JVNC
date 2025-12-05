package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @Column(nullable = false, unique = true, length = 50)
    private String promotionCode;

    private String promotionName;
    private String description;
    private String discountLevel; // ITEM / CART
    private String discountType; // PERCENT / AMOUNT
    private Double discountValue;
    private Integer maxUsesTotal;
    private Integer maxUsesPerUser;
    private Double minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive = true;
}
