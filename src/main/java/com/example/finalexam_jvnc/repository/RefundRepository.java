package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByStatus(String status);
    List<Refund> findByOrderOrderId(Long orderId);
    
    @Query("SELECT r FROM Refund r LEFT JOIN FETCH r.order LEFT JOIN FETCH r.payment ORDER BY r.requestedAt DESC")
    List<Refund> findAllWithOrderAndPayment();
    
    @Query("SELECT SUM(r.refundAmount) FROM Refund r WHERE r.status = 'APPROVED' AND r.completedAt BETWEEN :startDate AND :endDate")
    Double getTotalRefundAmountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}

