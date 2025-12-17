package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartDTO {
    private Long itemId;
    private Integer quantity;
}
