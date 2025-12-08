package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getOrdersByStatus(String status);
    OrderDTO updateOrderStatus(Long id, String status);
}

