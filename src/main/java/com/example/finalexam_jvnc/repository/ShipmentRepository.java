package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByOrderOrderId(Long orderId);
    List<Shipment> findByStatus(String status);
    
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.order ORDER BY s.shippedAt DESC")
    List<Shipment> findAllWithOrder();
}

