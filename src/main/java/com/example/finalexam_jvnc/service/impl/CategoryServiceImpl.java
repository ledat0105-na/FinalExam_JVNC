package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.CategoryDTO;
import com.example.finalexam_jvnc.model.Category;
import com.example.finalexam_jvnc.repository.CategoryRepository;
import com.example.finalexam_jvnc.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Check if categoryCode already exists
        if (categoryRepository.findByCategoryCode(categoryDTO.getCategoryCode()).isPresent()) {
            throw new RuntimeException("Category code already exists: " + categoryDTO.getCategoryCode());
        }

        Category category = Category.builder()
                .categoryCode(categoryDTO.getCategoryCode())
                .categoryName(categoryDTO.getCategoryName())
                .description(categoryDTO.getDescription())
                .isActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : true)
                .build();

        // Set parent category if provided
        if (categoryDTO.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if categoryCode is being changed and if it already exists
        if (!category.getCategoryCode().equals(categoryDTO.getCategoryCode())) {
            if (categoryRepository.findByCategoryCode(categoryDTO.getCategoryCode()).isPresent()) {
                throw new RuntimeException("Category code already exists: " + categoryDTO.getCategoryCode());
            }
        }

        category.setCategoryCode(categoryDTO.getCategoryCode());
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        if (categoryDTO.getIsActive() != null) {
            category.setIsActive(categoryDTO.getIsActive());
        }

        // Update parent category if provided
        if (categoryDTO.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryCode(category.getCategoryCode())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();

        if (category.getParent() != null) {
            dto.setParentCategoryId(category.getParent().getCategoryId());
            dto.setParentCategoryName(category.getParent().getCategoryName());
        }

        return dto;
    }
}

