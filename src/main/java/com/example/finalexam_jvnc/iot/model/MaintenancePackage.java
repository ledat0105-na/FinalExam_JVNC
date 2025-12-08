package com.example.finalexam_jvnc.iot.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MaintenancePackages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenancePackage {
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
    private String maintenanceItems; // Danh mục bảo trì (JSON)
    
    private Integer duration; // Thời gian bảo trì (ngày)
    private String frequency; // Tần suất: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

    private Boolean isActive = true;
}

