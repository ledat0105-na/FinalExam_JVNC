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

    @Override
    public void reserveStock(Long itemId, int quantity) {
        List<StockItem> stockItems = stockItemRepository.findByItemItemId(itemId);

        // Check total availability
        int totalAvailable = stockItems.stream()
                .mapToInt(si -> si.getQuantityOnHand() - si.getQuantityReserved())
                .sum();

        if (totalAvailable < quantity) {
            throw new RuntimeException("Insufficient stock for item ID: " + itemId);
        }

        int remainingToReserve = quantity;
        for (StockItem si : stockItems) {
            if (remainingToReserve <= 0)
                break;

            int available = si.getQuantityOnHand() - si.getQuantityReserved();
            if (available > 0) {
                int toReserve = Math.min(remainingToReserve, available);
                si.setQuantityReserved(si.getQuantityReserved() + toReserve);
                stockItemRepository.save(si);
                remainingToReserve -= toReserve;
            }
        }
    }

    @Override
    public void releaseStock(Long itemId, int quantity) {
        List<StockItem> stockItems = stockItemRepository.findByItemItemId(itemId);
        int remainingToRelease = quantity;

        for (StockItem si : stockItems) {
            if (remainingToRelease <= 0)
                break;

            if (si.getQuantityReserved() > 0) {
                int toRelease = Math.min(remainingToRelease, si.getQuantityReserved());
                si.setQuantityReserved(si.getQuantityReserved() - toRelease);
                stockItemRepository.save(si);
                remainingToRelease -= toRelease;
            }
        }
    }

    @Override
    public void deductStock(Long itemId, int quantity) {
        List<StockItem> stockItems = stockItemRepository.findByItemItemId(itemId);
        int remainingToDeduct = quantity;

        for (StockItem si : stockItems) {
            if (remainingToDeduct <= 0)
                break;

            // We assume reserved stock is being used.
            // If reserved > 0, we reduce both onHand and reserved.
            // If reserved == 0 (direct sale?), we reduce onHand.
            // Logic: Try to reduce from reserved first if available.

            int availableInReserved = si.getQuantityReserved();
            if (availableInReserved > 0) {
                int toDeduct = Math.min(remainingToDeduct, availableInReserved);
                si.setQuantityReserved(si.getQuantityReserved() - toDeduct);
                si.setQuantityOnHand(si.getQuantityOnHand() - toDeduct);
                stockItemRepository.save(si);
                remainingToDeduct -= toDeduct;
            } else {
                // Fallback: If for some reason nothing is reserved (or manual override), deduct
                // from onHand directly
                // Check onHand
                if (si.getQuantityOnHand() > 0) {
                    int toDeduct = Math.min(remainingToDeduct, si.getQuantityOnHand());
                    si.setQuantityOnHand(si.getQuantityOnHand() - toDeduct);
                    stockItemRepository.save(si);
                    remainingToDeduct -= toDeduct;
                }
            }
        }

        if (remainingToDeduct > 0) {
            throw new RuntimeException("Error deducting stock: Inconsistent state for item ID: " + itemId);
        }
    }
}
