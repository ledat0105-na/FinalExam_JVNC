package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.BestSellingItemDTO;
import com.example.finalexam_jvnc.dto.RevenueReportDTO;
import com.example.finalexam_jvnc.model.Item;
import com.example.finalexam_jvnc.repository.ItemRepository;
import com.example.finalexam_jvnc.repository.OrderItemRepository;
import com.example.finalexam_jvnc.repository.OrderRepository;
import com.example.finalexam_jvnc.repository.RefundRepository;
import com.example.finalexam_jvnc.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public RevenueReportDTO getRevenueReportByDay(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);

        Double totalRevenue = orderRepository.getTotalRevenueByDateRange(startDate, endDate);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrders = orderRepository.countOrdersByStatusAndDateRange("DONE", startDate, endDate);
        if (totalOrders == null) totalOrders = 0L;

        Double totalRefundAmount = refundRepository.getTotalRefundAmountByDateRange(startDate, endDate);
        if (totalRefundAmount == null) totalRefundAmount = 0.0;

        Long totalRefunds = refundRepository.findAll().stream()
                .filter(r -> r.getStatus() != null && r.getStatus().equals("APPROVED"))
                .filter(r -> r.getCompletedAt() != null && 
                        !r.getCompletedAt().isBefore(startDate) && 
                        !r.getCompletedAt().isAfter(endDate))
                .count();

        return RevenueReportDTO.builder()
                .period(date.toString())
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0.0)
                .totalRefundAmount(totalRefundAmount)
                .totalRefunds(totalRefunds)
                .build();
    }

    @Override
    public RevenueReportDTO getRevenueReportByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Double totalRevenue = orderRepository.getTotalRevenueByDateRange(startDateTime, endDateTime);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrders = orderRepository.countOrdersByStatusAndDateRange("DONE", startDateTime, endDateTime);
        if (totalOrders == null) totalOrders = 0L;

        Double totalRefundAmount = refundRepository.getTotalRefundAmountByDateRange(startDateTime, endDateTime);
        if (totalRefundAmount == null) totalRefundAmount = 0.0;

        Long totalRefunds = refundRepository.findAll().stream()
                .filter(r -> r.getStatus() != null && r.getStatus().equals("APPROVED"))
                .filter(r -> r.getCompletedAt() != null && 
                        !r.getCompletedAt().isBefore(startDateTime) && 
                        !r.getCompletedAt().isAfter(endDateTime))
                .count();

        return RevenueReportDTO.builder()
                .period(year + "-" + String.format("%02d", month))
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0.0)
                .totalRefundAmount(totalRefundAmount)
                .totalRefunds(totalRefunds)
                .build();
    }

    @Override
    public List<BestSellingItemDTO> getBestSellingItems(int limit) {
        List<Object[]> results = orderItemRepository.findBestSellingItems();
        
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Long itemId = (Long) result[0];
                    Long totalQuantity = (Long) result[1];
                    
                    Item item = itemRepository.findById(itemId).orElse(null);
                    if (item == null) return null;
                    
                    // Calculate total revenue for this item
                    Double totalRevenue = orderItemRepository.findAll().stream()
                            .filter(oi -> oi.getItem().getItemId().equals(itemId))
                            .filter(oi -> oi.getOrder().getStatus() != null && oi.getOrder().getStatus().equals("DONE"))
                            .mapToDouble(oi -> oi.getLineTotal() != null ? oi.getLineTotal() : 0.0)
                            .sum();
                    
                    return BestSellingItemDTO.builder()
                            .itemId(itemId)
                            .itemName(item.getItemName())
                            .sku(item.getSku())
                            .totalQuantitySold(totalQuantity)
                            .totalRevenue(totalRevenue)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public RevenueReportDTO getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Double totalRevenue = orderRepository.getTotalRevenueByDateRange(startDateTime, endDateTime);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrders = orderRepository.countOrdersByStatusAndDateRange("DONE", startDateTime, endDateTime);
        if (totalOrders == null) totalOrders = 0L;

        Double totalRefundAmount = refundRepository.getTotalRefundAmountByDateRange(startDateTime, endDateTime);
        if (totalRefundAmount == null) totalRefundAmount = 0.0;

        Long totalRefunds = refundRepository.findAll().stream()
                .filter(r -> r.getStatus() != null && r.getStatus().equals("APPROVED"))
                .filter(r -> r.getCompletedAt() != null && 
                        !r.getCompletedAt().isBefore(startDateTime) && 
                        !r.getCompletedAt().isAfter(endDateTime))
                .count();

        return RevenueReportDTO.builder()
                .period(startDate + " to " + endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0.0)
                .totalRefundAmount(totalRefundAmount)
                .totalRefunds(totalRefunds)
                .build();
    }
}

