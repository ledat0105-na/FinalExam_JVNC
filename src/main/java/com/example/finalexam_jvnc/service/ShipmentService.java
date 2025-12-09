package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.ShipmentDTO;

import java.util.List;

public interface ShipmentService {
    List<ShipmentDTO> getAllShipments();
    ShipmentDTO getShipmentById(Long id);
    List<ShipmentDTO> getShipmentsByOrder(Long orderId);
    List<ShipmentDTO> getShipmentsByStatus(String status);
    ShipmentDTO createShipment(Long orderId, String trackingNumber, String carrier, String status);
    ShipmentDTO updateShipmentStatus(Long id, String status, String trackingNumber, String carrier);
}

