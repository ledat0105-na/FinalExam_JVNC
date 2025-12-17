package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByPromotionCode(String promotionCode);
    List<Promotion> findByIsActiveTrue();
}

