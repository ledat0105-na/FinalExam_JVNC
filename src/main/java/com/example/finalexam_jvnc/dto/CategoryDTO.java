package com.example.finalexam_jvnc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Long parentCategoryId;
    private String parentCategoryName;
    private String description;
    private Boolean isActive;
}

