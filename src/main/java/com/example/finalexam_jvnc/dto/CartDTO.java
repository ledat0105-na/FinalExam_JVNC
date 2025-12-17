package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private Long cartId;
    private List<CartItemDTO> items;
    private Double totalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String appliedCouponCode;
}
