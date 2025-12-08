package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.ShipmentDTO;
import com.example.finalexam_jvnc.model.Shipment;
import com.example.finalexam_jvnc.repository.ShipmentRepository;
import com.example.finalexam_jvnc.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Override
    public List<ShipmentDTO> getAllShipments() {
        return shipmentRepository.findAllWithOrder().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentDTO getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        return convertToDTO(shipment);
    }

    @Override
    public List<ShipmentDTO> getShipmentsByOrder(Long orderId) {
        return shipmentRepository.findByOrderOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShipmentDTO> getShipmentsByStatus(String status) {
        return shipmentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentDTO updateShipmentStatus(Long id, String status, String trackingNumber, String carrier) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + id));
        
        shipment.setStatus(status);
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            shipment.setTrackingNumber(trackingNumber);
        }
        if (carrier != null && !carrier.isEmpty()) {
            shipment.setCarrier(carrier);
        }
        
        if ("SHIPPED".equals(status) && shipment.getShippedAt() == null) {
            shipment.setShippedAt(LocalDateTime.now());
        }
        if ("DELIVERED".equals(status) && shipment.getDeliveredAt() == null) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }
        
        shipment = shipmentRepository.save(shipment);
        return convertToDTO(shipment);
    }

    private ShipmentDTO convertToDTO(Shipment shipment) {
        return ShipmentDTO.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrder().getOrderId())
                .orderNumber(shipment.getOrder().getOrderNumber())
                .trackingNumber(shipment.getTrackingNumber())
                .carrier(shipment.getCarrier())
                .status(shipment.getStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .build();
    }
}

