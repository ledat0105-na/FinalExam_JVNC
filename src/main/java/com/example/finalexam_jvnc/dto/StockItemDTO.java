package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItemDTO {
    private Long stockItemId;
    private Long warehouseId;
    private String warehouseName;
    private Long itemId;
    private String itemName;
    private String itemSku;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer lowStockThreshold;
    private Integer availableQuantity;
}

