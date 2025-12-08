package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.PromotionUsageDTO;

import java.util.List;

public interface PromotionUsageService {
    List<PromotionUsageDTO> getAllPromotionUsages();
    List<PromotionUsageDTO> getUsagesByPromotion(Long promotionId);
    List<PromotionUsageDTO> getUsagesByAccount(Long accountId);
}

