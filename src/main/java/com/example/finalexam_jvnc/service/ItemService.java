package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.ItemDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    List<ItemDTO> getAllItems();
    ItemDTO getItemById(Long id);
    ItemDTO getItemBySku(String sku);
    ItemDTO createItem(ItemDTO itemDTO);
    ItemDTO updateItem(Long id, ItemDTO itemDTO);
    void deleteItem(Long id);
    List<ItemDTO> getActiveItems();
    List<ItemDTO> importFromCSV(MultipartFile file);
    byte[] exportToCSV();
}

