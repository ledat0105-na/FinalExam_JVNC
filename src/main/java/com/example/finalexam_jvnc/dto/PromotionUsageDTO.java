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
public class PromotionUsageDTO {
    private Long promotionUsageId;
    private Long promotionId;
    private String promotionCode;
    private Long orderId;
    private String orderNumber;
    private Long accountId;
    private String accountUsername;
    private Double discountAmount;
    private LocalDateTime usedAt;
}

