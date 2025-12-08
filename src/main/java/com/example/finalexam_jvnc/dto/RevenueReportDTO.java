package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportDTO {
    private String period; // Day or Month
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private Double totalRefundAmount;
    private Long totalRefunds;
}

