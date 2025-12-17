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

    @Autowired
    private com.example.finalexam_jvnc.repository.WalletRepository walletRepository;

    @Autowired
    private com.example.finalexam_jvnc.service.NotificationService notificationService;

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

        if (!"PENDING".equals(refund.getStatus()) && !"REQUESTED".equals(refund.getStatus())) {
            throw new RuntimeException("Only pending or requested refunds can be approved");
        }

        processWalletRefund(refund);
        sendRefundApprovalNotification(refund);

        refund.setStatus("APPROVED");
        refund.setCompletedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);
        return convertToDTO(refund);
    }

    @Override
    public RefundDTO rejectRefund(Long id, String reason) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));

        if (!"PENDING".equals(refund.getStatus()) && !"REQUESTED".equals(refund.getStatus())) {
            throw new RuntimeException("Only pending or requested refunds can be rejected");
        }

        refund.setStatus("REJECTED");
        if (reason != null && !reason.isEmpty()) {
            refund.setReason(refund.getReason() + " [Rejected: " + reason + "]");
        }
        refund.setCompletedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);
        return convertToDTO(refund);
    }

    @Override
    public RefundDTO updateRefundStatus(Long id, String status) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));

        // Validate status
        List<String> validStatuses = List.of("PENDING", "REQUESTED", "APPROVED", "REJECTED", "PROCESSING", "COMPLETED",
                "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid refund status: " + status);
        }

        if ("APPROVED".equals(status) && !"APPROVED".equals(refund.getStatus())) {
            processWalletRefund(refund);
            sendRefundApprovalNotification(refund);
        }

        refund.setStatus(status);

        // Set completedAt for terminal statuses
        if ("APPROVED".equals(status) || "REJECTED".equals(status) || "COMPLETED".equals(status)
                || "CANCELLED".equals(status)) {
            if (refund.getCompletedAt() == null) {
                refund.setCompletedAt(LocalDateTime.now());
            }
        } else {
            // Reset completedAt for non-terminal statuses
            refund.setCompletedAt(null);
        }

        refund = refundRepository.save(refund);
        return convertToDTO(refund);
    }

    private void processWalletRefund(Refund refund) {
        com.example.finalexam_jvnc.model.Order order = refund.getOrder();
        if ("WALLET".equals(order.getPaymentMethod())) {
            com.example.finalexam_jvnc.model.Account customer = order.getCustomer();
            com.example.finalexam_jvnc.model.Wallet wallet = walletRepository
                    .findByAccount_Username(customer.getUsername())
                    .orElseThrow(
                            () -> new RuntimeException("Wallet not found for customer: " + customer.getUsername()));

            // Credit the amount back to wallet
            wallet.setBalance(wallet.getBalance() + refund.getRefundAmount());
            walletRepository.save(wallet);
        }
    }

    private void sendRefundApprovalNotification(Refund refund) {
        try {
            com.example.finalexam_jvnc.model.Account customer = refund.getOrder().getCustomer();
            String message = String.format(
                    "Yêu cầu hoàn tiền cho đơn hàng #%s đã được chấp nhận. Số tiền %.0f đ đã được hoàn lại vào ví của bạn.",
                    refund.getOrder().getOrderNumber(), refund.getRefundAmount());

            notificationService.createNotification(
                    customer,
                    "Hoàn tiền thành công",
                    message,
                    com.example.finalexam_jvnc.model.Notification.NotificationType.SUCCESS,
                    "/account/orders/" + refund.getOrder().getOrderId());
        } catch (Exception e) {
            // Log error but don't fail transaction
            System.err.println("Failed to send notification: " + e.getMessage());
        }
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
