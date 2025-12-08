package com.example.finalexam_jvnc.healthcare.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MedicalPackages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalPackage {
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

    @Lob
    private String description;
    
    @Lob
    private String includedServices; // Danh sách dịch vụ (JSON)
    
    @Lob
    private String requirements; // Yêu cầu trước khi khám (JSON)

    private Boolean isActive = true;
}

