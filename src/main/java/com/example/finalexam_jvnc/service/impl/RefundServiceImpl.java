package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.RefundDTO;
import com.example.finalexam_jvnc.model.Refund;
import com.example.finalexam_jvnc.repository.RefundRepository;
import com.example.finalexam_jvnc.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Override
    public List<RefundDTO> getAllRefunds() {
        return refundRepository.findAllWithOrderAndPayment().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RefundDTO getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));
        return convertToDTO(refund);
    }

    @Override
    public List<RefundDTO> getRefundsByStatus(String status) {
        return refundRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RefundDTO approveRefund(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));
        
        if (!"PENDING".equals(refund.getStatus())) {
            throw new RuntimeException("Only pending refunds can be approved");
        }
        
        refund.setStatus("APPROVED");
        refund.setCompletedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);
        return convertToDTO(refund);
    }

    @Override
    public RefundDTO rejectRefund(Long id, String reason) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));
        
        if (!"PENDING".equals(refund.getStatus())) {
            throw new RuntimeException("Only pending refunds can be rejected");
        }
        
        refund.setStatus("REJECTED");
        if (reason != null && !reason.isEmpty()) {
            refund.setReason(refund.getReason() + " [Rejected: " + reason + "]");
        }
        refund.setCompletedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);
        return convertToDTO(refund);
    }

    private RefundDTO convertToDTO(Refund refund) {
        return RefundDTO.builder()
                .refundId(refund.getRefundId())
                .orderId(refund.getOrder().getOrderId())
                .orderNumber(refund.getOrder().getOrderNumber())
                .paymentId(refund.getPayment() != null ? refund.getPayment().getPaymentId() : null)
                .refundAmount(refund.getRefundAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }
}

