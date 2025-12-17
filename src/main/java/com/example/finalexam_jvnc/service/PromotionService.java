package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.PromotionDTO;

import java.util.List;

public interface PromotionService {
    List<PromotionDTO> getAllPromotions();
    PromotionDTO getPromotionById(Long id);
    PromotionDTO createPromotion(PromotionDTO promotionDTO);
    PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO);
    void deletePromotion(Long id);
    List<PromotionDTO> getActivePromotions();
}

