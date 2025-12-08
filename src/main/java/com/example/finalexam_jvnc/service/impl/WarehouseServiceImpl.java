package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.WarehouseDTO;
import com.example.finalexam_jvnc.model.Warehouse;
import com.example.finalexam_jvnc.repository.WarehouseRepository;
import com.example.finalexam_jvnc.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));
        return convertToDTO(warehouse);
    }

    @Override
    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        // Check if warehouseCode already exists
        if (warehouseRepository.findByWarehouseCode(warehouseDTO.getWarehouseCode()).isPresent()) {
            throw new RuntimeException("Warehouse code already exists: " + warehouseDTO.getWarehouseCode());
        }

        Warehouse warehouse = Warehouse.builder()
                .warehouseCode(warehouseDTO.getWarehouseCode())
                .warehouseName(warehouseDTO.getWarehouseName())
                .addressLine(warehouseDTO.getAddressLine())
                .city(warehouseDTO.getCity())
                .country(warehouseDTO.getCountry())
                .isActive(warehouseDTO.getIsActive() != null ? warehouseDTO.getIsActive() : true)
                .build();

        warehouse = warehouseRepository.save(warehouse);
        return convertToDTO(warehouse);
    }

    @Override
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));

        // Check if warehouseCode is being changed and if it already exists
        if (!warehouse.getWarehouseCode().equals(warehouseDTO.getWarehouseCode())) {
            if (warehouseRepository.findByWarehouseCode(warehouseDTO.getWarehouseCode()).isPresent()) {
                throw new RuntimeException("Warehouse code already exists: " + warehouseDTO.getWarehouseCode());
            }
        }

        warehouse.setWarehouseCode(warehouseDTO.getWarehouseCode());
        warehouse.setWarehouseName(warehouseDTO.getWarehouseName());
        warehouse.setAddressLine(warehouseDTO.getAddressLine());
        warehouse.setCity(warehouseDTO.getCity());
        warehouse.setCountry(warehouseDTO.getCountry());
        if (warehouseDTO.getIsActive() != null) {
            warehouse.setIsActive(warehouseDTO.getIsActive());
        }

        warehouse = warehouseRepository.save(warehouse);
        return convertToDTO(warehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));
        warehouseRepository.delete(warehouse);
    }

    @Override
    public List<WarehouseDTO> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        return WarehouseDTO.builder()
                .warehouseId(warehouse.getWarehouseId())
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .addressLine(warehouse.getAddressLine())
                .city(warehouse.getCity())
                .country(warehouse.getCountry())
                .isActive(warehouse.getIsActive())
                .build();
    }
}

