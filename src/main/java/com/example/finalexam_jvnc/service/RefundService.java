package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.RefundDTO;

import java.util.List;

public interface RefundService {
    List<RefundDTO> getAllRefunds();
    RefundDTO getRefundById(Long id);
    List<RefundDTO> getRefundsByStatus(String status);
    RefundDTO approveRefund(Long id);
    RefundDTO rejectRefund(Long id, String reason);
    RefundDTO updateRefundStatus(Long id, String status);
}

