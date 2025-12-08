package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.PromotionUsageDTO;
import com.example.finalexam_jvnc.model.PromotionUsage;
import com.example.finalexam_jvnc.repository.PromotionUsageRepository;
import com.example.finalexam_jvnc.service.PromotionUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionUsageServiceImpl implements PromotionUsageService {

    @Autowired
    private PromotionUsageRepository promotionUsageRepository;

    @Override
    public List<PromotionUsageDTO> getAllPromotionUsages() {
        return promotionUsageRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionUsageDTO> getUsagesByPromotion(Long promotionId) {
        return promotionUsageRepository.findByPromotionPromotionId(promotionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionUsageDTO> getUsagesByAccount(Long accountId) {
        return promotionUsageRepository.findByAccountAccountId(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PromotionUsageDTO convertToDTO(PromotionUsage usage) {
        return PromotionUsageDTO.builder()
                .promotionUsageId(usage.getPromotionUsageId())
                .promotionId(usage.getPromotion().getPromotionId())
                .promotionCode(usage.getPromotion().getPromotionCode())
                .orderId(usage.getOrder() != null ? usage.getOrder().getOrderId() : null)
                .orderNumber(usage.getOrder() != null ? usage.getOrder().getOrderNumber() : null)
                .accountId(usage.getAccount().getAccountId())
                .accountUsername(usage.getAccount().getUsername())
                .discountAmount(usage.getDiscountAmount())
                .usedAt(usage.getUsedAt())
                .build();
    }
}

