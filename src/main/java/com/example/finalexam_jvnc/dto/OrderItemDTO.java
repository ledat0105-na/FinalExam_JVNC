package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long orderItemId;
    private Long itemId;
    private String itemName;
    private String sku;
    private Integer quantity;
    private Double unitPrice;
    private Double discountAmount;
    private Double lineTotal;
}
