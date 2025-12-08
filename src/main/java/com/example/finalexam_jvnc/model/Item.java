package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @Column(nullable=false, unique=true, length=50)
    private String sku;

    @Column(nullable=false, length=200)
    private String itemName;

    @Column(nullable=false, length=20)
    private String itemType; // PRODUCT / SERVICE

    @Column(nullable=false, length=50)
    private String unitName;

    @Column(nullable=false)
    private Double unitPrice;

    private Double weightKg;

    @Lob
    private String description;

    private Boolean isActive = true;
}

