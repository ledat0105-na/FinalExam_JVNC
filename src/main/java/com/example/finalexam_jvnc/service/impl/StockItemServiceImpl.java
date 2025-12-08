package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.StockItemDTO;
import com.example.finalexam_jvnc.model.StockItem;
import com.example.finalexam_jvnc.repository.StockItemRepository;
import com.example.finalexam_jvnc.service.StockItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockItemServiceImpl implements StockItemService {

    @Autowired
    private StockItemRepository stockItemRepository;

    @Override
    public List<StockItemDTO> getAllStockItems() {
        return stockItemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StockItemDTO getStockItemById(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with id: " + id));
        return convertToDTO(stockItem);
    }

    @Override
    public StockItemDTO updateStockQuantity(Long id, Integer quantityOnHand, Integer lowStockThreshold) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock item not found with id: " + id));
        
        if (quantityOnHand != null) {
            stockItem.setQuantityOnHand(quantityOnHand);
        }
        if (lowStockThreshold != null) {
            stockItem.setLowStockThreshold(lowStockThreshold);
        }
        
        stockItem = stockItemRepository.save(stockItem);
        return convertToDTO(stockItem);
    }

    @Override
    public List<StockItemDTO> getLowStockItems() {
        return stockItemRepository.findLowStockItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemDTO> getStockItemsByWarehouse(Long warehouseId) {
        return stockItemRepository.findByWarehouseWarehouseId(warehouseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemDTO> getStockItemsByItem(Long itemId) {
        return stockItemRepository.findByItemItemId(itemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private StockItemDTO convertToDTO(StockItem stockItem) {
        StockItemDTO dto = StockItemDTO.builder()
                .stockItemId(stockItem.getStockItemId())
                .warehouseId(stockItem.getWarehouse().getWarehouseId())
                .warehouseName(stockItem.getWarehouse().getWarehouseName())
                .itemId(stockItem.getItem().getItemId())
                .itemName(stockItem.getItem().getItemName())
                .itemSku(stockItem.getItem().getSku())
                .quantityOnHand(stockItem.getQuantityOnHand())
                .quantityReserved(stockItem.getQuantityReserved())
                .lowStockThreshold(stockItem.getLowStockThreshold())
                .build();
        
        dto.setAvailableQuantity(stockItem.getQuantityOnHand() - stockItem.getQuantityReserved());
        return dto;
    }
}

