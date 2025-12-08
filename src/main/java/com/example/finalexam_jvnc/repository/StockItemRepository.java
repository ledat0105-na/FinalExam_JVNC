package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    Optional<StockItem> findByWarehouseWarehouseIdAndItemItemId(Long warehouseId, Long itemId);
    List<StockItem> findByItemItemId(Long itemId);
    List<StockItem> findByWarehouseWarehouseId(Long warehouseId);
    
    @Query("SELECT si FROM StockItem si WHERE si.quantityOnHand <= si.lowStockThreshold AND si.lowStockThreshold > 0")
    List<StockItem> findLowStockItems();
}

