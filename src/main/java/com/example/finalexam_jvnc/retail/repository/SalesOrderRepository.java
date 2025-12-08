package com.example.finalexam_jvnc.retail.repository;

import com.example.finalexam_jvnc.retail.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
    List<SalesOrder> findByStatus(String status);
    List<SalesOrder> findByCustomerAccountId(Long customerId);
    
    @Query("SELECT o FROM SalesOrder o LEFT JOIN FETCH o.customer ORDER BY o.createdAt DESC")
    List<SalesOrder> findAllWithCustomer();
}

