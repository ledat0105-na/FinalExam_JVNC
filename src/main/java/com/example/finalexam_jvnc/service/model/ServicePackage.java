package com.example.finalexam_jvnc.service.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ServicePackages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServicePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    @ManyToOne
    @JoinColumn(name = "CategoryId")
    private Category category;

    @Column(nullable = false, unique = true, length = 50)
    private String packageCode;

    @Column(nullable = false, length = 200)
    private String packageName;

    @Column(nullable = false)
    private Double packagePrice;

    @Column(length = 50)
    private String unitName; // "Buổi", "Gói", "Tháng"

    @Lob
    private String description;
    
    @Lob
    private String serviceDetails; // Chi tiết dịch vụ (JSON)
    
    private Integer duration; // Thời lượng (phút/buổi)

    private Boolean isActive = true;
}

