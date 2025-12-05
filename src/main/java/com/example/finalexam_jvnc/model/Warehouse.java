package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Warehouses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warehouseId;

    @Column(nullable=false, unique=true, length=50)
    private String warehouseCode;

    @Column(nullable=false, length=150)
    private String warehouseName;

    private String addressLine;
    private String city;
    private String country;
    private Boolean isActive = true;
}

