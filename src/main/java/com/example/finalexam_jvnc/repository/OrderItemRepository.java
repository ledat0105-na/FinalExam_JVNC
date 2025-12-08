package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderOrderId(Long orderId);
    
    @Query("SELECT oi.item.itemId, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DONE' " +
           "GROUP BY oi.item.itemId " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findBestSellingItems();
}

