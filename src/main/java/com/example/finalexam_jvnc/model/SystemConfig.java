package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SystemConfigs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    @Column(nullable = false, unique = true, length = 100)
    private String configKey; // SHIPPING_FEE, TAX_RATE, COD_FEE, GATEWAY_FEE

    @Column(nullable = false, length = 200)
    private String configName;

    @Column(columnDefinition = "TEXT")
    private String configValue; // Can store JSON or simple value

    private String description;
    private Boolean isActive = true;
}

