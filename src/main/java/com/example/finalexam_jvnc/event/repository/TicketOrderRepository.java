package com.example.finalexam_jvnc.event.repository;

import com.example.finalexam_jvnc.event.model.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {
    Optional<TicketOrder> findByOrderNumber(String orderNumber);
    List<TicketOrder> findByStatus(String status);
    List<TicketOrder> findByCustomerAccountId(Long customerId);
    List<TicketOrder> findByTicketTicketId(Long ticketId);
    
    @Query("SELECT t FROM TicketOrder t LEFT JOIN FETCH t.customer LEFT JOIN FETCH t.ticket ORDER BY t.orderDate DESC")
    List<TicketOrder> findAllWithDetails();
}

