package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.OrderDTO;
import com.example.finalexam_jvnc.model.Order;
import com.example.finalexam_jvnc.repository.OrderRepository;
import com.example.finalexam_jvnc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllWithCustomer().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Validate status
        List<String> validStatuses = List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "DONE", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid order status: " + status);
        }
        
        order.setStatus(status);
        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getAccountId())
                .customerUsername(order.getCustomer().getUsername())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxAmount(order.getTaxAmount())
                .shippingFee(order.getShippingFee())
                .codFee(order.getCodFee())
                .gatewayFee(order.getGatewayFee())
                .grandTotal(order.getGrandTotal())
                .amountDue(order.getAmountDue())
                .createdAt(order.getCreatedAt())
                .build();
    }
}

