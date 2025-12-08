package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryCode(String categoryCode);
    List<Category> findByIsActiveTrue();
    List<Category> findByParentIsNull();
}

