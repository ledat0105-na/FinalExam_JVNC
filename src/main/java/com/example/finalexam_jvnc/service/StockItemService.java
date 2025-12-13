package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.StockItemDTO;

import java.util.List;

public interface StockItemService {
    List<StockItemDTO> getAllStockItems();

    StockItemDTO getStockItemById(Long id);

    StockItemDTO updateStockQuantity(Long id, Integer quantityOnHand, Integer lowStockThreshold);

    List<StockItemDTO> getLowStockItems();

    List<StockItemDTO> getStockItemsByWarehouse(Long warehouseId);

    List<StockItemDTO> getStockItemsByItem(Long itemId);

    // Inventory Logic
    void reserveStock(Long itemId, int quantity);

    void releaseStock(Long itemId, int quantity);

    void deductStock(Long itemId, int quantity);
}
