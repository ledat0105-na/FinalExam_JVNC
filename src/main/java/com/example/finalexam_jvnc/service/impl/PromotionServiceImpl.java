package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.PromotionDTO;
import com.example.finalexam_jvnc.model.Promotion;
import com.example.finalexam_jvnc.repository.PromotionRepository;
import com.example.finalexam_jvnc.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDTO getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return convertToDTO(promotion);
    }

    @Override
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        if (promotionRepository.findByPromotionCode(promotionDTO.getPromotionCode()).isPresent()) {
            throw new RuntimeException("Promotion code already exists: " + promotionDTO.getPromotionCode());
        }

        Promotion promotion = Promotion.builder()
                .promotionCode(promotionDTO.getPromotionCode())
                .promotionName(promotionDTO.getPromotionName())
                .description(promotionDTO.getDescription())
                .discountLevel(promotionDTO.getDiscountLevel())
                .discountType(promotionDTO.getDiscountType())
                .discountValue(promotionDTO.getDiscountValue())
                .maxUsesTotal(promotionDTO.getMaxUsesTotal())
                .maxUsesPerUser(promotionDTO.getMaxUsesPerUser())
                .minOrderAmount(promotionDTO.getMinOrderAmount())
                .startDate(promotionDTO.getStartDate())
                .endDate(promotionDTO.getEndDate())
                .isActive(promotionDTO.getIsActive() != null ? promotionDTO.getIsActive() : true)
                .build();

        promotion = promotionRepository.save(promotion);
        return convertToDTO(promotion);
    }

    @Override
    public PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (!promotion.getPromotionCode().equals(promotionDTO.getPromotionCode())) {
            if (promotionRepository.findByPromotionCode(promotionDTO.getPromotionCode()).isPresent()) {
                throw new RuntimeException("Promotion code already exists: " + promotionDTO.getPromotionCode());
            }
        }

        promotion.setPromotionCode(promotionDTO.getPromotionCode());
        promotion.setPromotionName(promotionDTO.getPromotionName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setDiscountLevel(promotionDTO.getDiscountLevel());
        promotion.setDiscountType(promotionDTO.getDiscountType());
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setMaxUsesTotal(promotionDTO.getMaxUsesTotal());
        promotion.setMaxUsesPerUser(promotionDTO.getMaxUsesPerUser());
        promotion.setMinOrderAmount(promotionDTO.getMinOrderAmount());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        if (promotionDTO.getIsActive() != null) {
            promotion.setIsActive(promotionDTO.getIsActive());
        }

        promotion = promotionRepository.save(promotion);
        return convertToDTO(promotion);
    }

    @Override
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        promotionRepository.delete(promotion);
    }

    @Override
    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return PromotionDTO.builder()
                .promotionId(promotion.getPromotionId())
                .promotionCode(promotion.getPromotionCode())
                .promotionName(promotion.getPromotionName())
                .description(promotion.getDescription())
                .discountLevel(promotion.getDiscountLevel())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxUsesTotal(promotion.getMaxUsesTotal())
                .maxUsesPerUser(promotion.getMaxUsesPerUser())
                .minOrderAmount(promotion.getMinOrderAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .build();
    }
}

