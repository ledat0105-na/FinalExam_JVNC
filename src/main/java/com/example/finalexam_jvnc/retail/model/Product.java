package com.example.finalexam_jvnc.retail.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 50)
    private String unitName;

    @Column(nullable = false)
    private Double unitPrice;

    private Double weightKg;
    
    @Lob
    private String description;

    private Boolean isActive = true;
}

