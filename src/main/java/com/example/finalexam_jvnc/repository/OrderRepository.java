package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(String status);

    List<Order> findByCustomerAccountId(Long customerId);

    List<Order> findByCustomer_UsernameOrderByCreatedAtDesc(String username);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer ORDER BY o.createdAt DESC")
    List<Order> findAllWithCustomer();

    @Query("SELECT SUM(o.grandTotal) FROM Order o WHERE o.status = 'DONE' AND o.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersByStatusAndDateRange(String status, LocalDateTime startDate, LocalDateTime endDate);
}
