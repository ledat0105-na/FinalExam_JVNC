package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable=false, unique=true, length=50)
    private String categoryCode;

    @Column(nullable=false, length=150)
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "parentCategoryId")
    private Category parent;

    private String description;
    private Boolean isActive = true;
}

