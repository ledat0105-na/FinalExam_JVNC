package com.example.finalexam_jvnc.education.model;

import com.example.finalexam_jvnc.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @ManyToOne
    @JoinColumn(name = "CategoryId")
    private Category category;

    @Column(nullable = false, unique = true, length = 50)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String courseName;

    @Column(length = 50)
    private String unitName; // "Buổi", "Tháng", "Năm"

    @Column(nullable = false)
    private Double tuitionFee; // Học phí

    private Integer duration; // Thời lượng (số buổi/tháng)
    
    @Lob
    private String description;
    
    @Lob
    private String schedule; // Lịch học (JSON)
    
    @Lob
    private String curriculum; // Chương trình học (JSON)

    private Boolean isActive = true;
}

