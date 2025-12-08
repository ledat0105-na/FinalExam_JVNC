package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {
    List<PromotionUsage> findByPromotionPromotionId(Long promotionId);
    List<PromotionUsage> findByAccountAccountId(Long accountId);
    
    @Query("SELECT pu FROM PromotionUsage pu LEFT JOIN FETCH pu.promotion LEFT JOIN FETCH pu.order LEFT JOIN FETCH pu.account ORDER BY pu.usedAt DESC")
    List<PromotionUsage> findAllWithDetails();
}

