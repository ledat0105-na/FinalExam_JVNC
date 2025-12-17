package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.model.Order;

public interface OrderService {
    Order createOrderFromCart(String username, String paymentMethod);

    java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getOrdersByStatus(String status);

    java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getAllOrders();

    void updateOrderStatus(Long orderId, String status);

    java.util.List<com.example.finalexam_jvnc.dto.OrderDTO> getOrdersByCustomer(String username);

    com.example.finalexam_jvnc.dto.OrderDTO getOrderById(Long orderId);

    void cancelOrder(Long orderId, String username);

    void requestRefund(Long orderId, String username);
}
