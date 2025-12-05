package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PromotionUsages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionUsageId;

    @ManyToOne @JoinColumn(name = "PromotionId")
    private Promotion promotion;

    @ManyToOne @JoinColumn(name = "OrderId")
    private Order order;

    @ManyToOne @JoinColumn(name = "AccountId")
    private Account account;

    private Double discountAmount;
    private LocalDateTime usedAt;
}
