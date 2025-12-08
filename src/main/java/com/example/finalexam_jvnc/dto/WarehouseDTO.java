package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String addressLine;
    private String city;
    private String country;
    private Boolean isActive;
}

