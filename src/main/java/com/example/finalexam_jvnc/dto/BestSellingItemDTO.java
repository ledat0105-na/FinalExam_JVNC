package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestSellingItemDTO {
    private Long itemId;
    private String itemName;
    private String sku;
    private Long totalQuantitySold;
    private Double totalRevenue;
}

