package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Long cartItemId;
    private Long itemId;
    private String itemName;
    private String sku;
    private String unitName;
    private Double unitPrice;
    private Integer quantity;
    private Double totalPrice;
    private String imgUrl; // Placeholder for image if needed
}
