package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {
    private Long promotionId;
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
    private Boolean isActive;
}

