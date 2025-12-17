package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.WarehouseDTO;

import java.util.List;

public interface WarehouseService {
    List<WarehouseDTO> getAllWarehouses();
    WarehouseDTO getWarehouseById(Long id);
    WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO);
    WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO);
    void deleteWarehouse(Long id);
    List<WarehouseDTO> getActiveWarehouses();
}

