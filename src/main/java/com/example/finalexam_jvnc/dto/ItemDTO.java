package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {
    private Long itemId;
    private Long categoryId;
    private String categoryName;
    private String sku;
    private String itemName;
    private String itemType;
    private String unitName;
    private Double unitPrice;
    private Double weightKg;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Boolean hasImage;
}
