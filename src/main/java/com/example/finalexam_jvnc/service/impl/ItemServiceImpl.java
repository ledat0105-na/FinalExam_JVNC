package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.ItemDTO;
import com.example.finalexam_jvnc.model.Category;
import com.example.finalexam_jvnc.model.Item;
import com.example.finalexam_jvnc.repository.CategoryRepository;
import com.example.finalexam_jvnc.repository.ItemRepository;
import com.example.finalexam_jvnc.service.ItemService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAllWithCategory().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
        return convertToDTO(item);
    }

    @Override
    public ItemDTO getItemBySku(String sku) {
        Item item = itemRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Item not found with SKU: " + sku));
        return convertToDTO(item);
    }

    @Override
    public ItemDTO createItem(ItemDTO itemDTO) {
        // Check if SKU already exists
        if (itemRepository.findBySku(itemDTO.getSku()).isPresent()) {
            throw new RuntimeException("SKU already exists: " + itemDTO.getSku());
        }

        Category category = categoryRepository.findById(itemDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Item item = Item.builder()
                .category(category)
                .sku(itemDTO.getSku())
                .itemName(itemDTO.getItemName())
                .itemType(itemDTO.getItemType())
                .unitName(itemDTO.getUnitName())
                .unitPrice(itemDTO.getUnitPrice())
                .weightKg(itemDTO.getWeightKg())
                .description(itemDTO.getDescription())
                .imageUrl(itemDTO.getImageUrl())
                .isActive(itemDTO.getIsActive() != null ? itemDTO.getIsActive() : true)
                .build();

        item = itemRepository.save(item);
        return convertToDTO(item);
    }

    @Override
    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        // Check if SKU is being changed and if it already exists
        if (!item.getSku().equals(itemDTO.getSku())) {
            if (itemRepository.findBySku(itemDTO.getSku()).isPresent()) {
                throw new RuntimeException("SKU already exists: " + itemDTO.getSku());
            }
        }

        Category category = categoryRepository.findById(itemDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        item.setCategory(category);
        item.setSku(itemDTO.getSku());
        item.setItemName(itemDTO.getItemName());
        item.setItemType(itemDTO.getItemType());
        item.setUnitName(itemDTO.getUnitName());
        item.setUnitPrice(itemDTO.getUnitPrice());
        item.setWeightKg(itemDTO.getWeightKg());
        item.setDescription(itemDTO.getDescription());
        if (itemDTO.getImageUrl() != null) {
            item.setImageUrl(itemDTO.getImageUrl());
        }
        if (itemDTO.getIsActive() != null) {
            item.setIsActive(itemDTO.getIsActive());
        }

        item = itemRepository.save(item);
        return convertToDTO(item);
    }

    @Override
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
        itemRepository.delete(item);
    }

    @Override
    public List<ItemDTO> getActiveItems() {
        return itemRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> searchItems(String keyword, Long categoryId) {
        return itemRepository.searchActiveItems(keyword, categoryId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDTO> importFromCSV(MultipartFile file) {
        List<ItemDTO> importedItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> records = csvReader.readAll();

            // Skip header row
            if (records.isEmpty()) {
                throw new RuntimeException("CSV file is empty");
            }

            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                try {
                    if (record.length < 8) {
                        errors.add("Row " + (i + 1) + ": Insufficient columns");
                        continue;
                    }

                    ItemDTO itemDTO = ItemDTO.builder()
                            .sku(record[0].trim())
                            .itemName(record[1].trim())
                            .categoryId(Long.parseLong(record[2].trim()))
                            .itemType(record[3].trim())
                            .unitName(record[4].trim())
                            .unitPrice(Double.parseDouble(record[5].trim()))
                            .weightKg(record[6].isEmpty() ? null : Double.parseDouble(record[6].trim()))
                            .description(record.length > 7 ? record[7].trim() : "")
                            .isActive(true)
                            .build();

                    // Check if item exists by SKU
                    Item existingItem = itemRepository.findBySku(itemDTO.getSku()).orElse(null);
                    if (existingItem != null) {
                        // Update existing item
                        itemDTO.setItemId(existingItem.getItemId());
                        ItemDTO updated = updateItem(existingItem.getItemId(), itemDTO);
                        importedItems.add(updated);
                    } else {
                        // Create new item
                        ItemDTO created = createItem(itemDTO);
                        importedItems.add(created);
                    }
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new RuntimeException("Import completed with errors: " + String.join("; ", errors));
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (CsvException e) {
            throw new RuntimeException("Error parsing CSV file: " + e.getMessage(), e);
        }

        return importedItems;
    }

    @Override
    public byte[] exportToCSV() {
        List<ItemDTO> items = getAllItems();

        try (StringWriter writer = new StringWriter();
                CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            csvWriter.writeNext(new String[] {
                    "SKU", "Item Name", "Category ID", "Category Name", "Item Type",
                    "Unit Name", "Unit Price", "Weight (Kg)", "Description", "Is Active"
            });

            // Write data
            for (ItemDTO item : items) {
                csvWriter.writeNext(new String[] {
                        item.getSku(),
                        item.getItemName(),
                        item.getCategoryId().toString(),
                        item.getCategoryName(),
                        item.getItemType(),
                        item.getUnitName(),
                        item.getUnitPrice().toString(),
                        item.getWeightKg() != null ? item.getWeightKg().toString() : "",
                        item.getDescription() != null ? item.getDescription() : "",
                        item.getIsActive() != null ? item.getIsActive().toString() : "true"
                });
            }

            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting to CSV: " + e.getMessage(), e);
        }
    }

    private ItemDTO convertToDTO(Item item) {
        return ItemDTO.builder()
                .itemId(item.getItemId())
                .categoryId(item.getCategory().getCategoryId())
                .categoryName(item.getCategory().getCategoryName())
                .sku(item.getSku())
                .itemName(item.getItemName())
                .itemType(item.getItemType())
                .unitName(item.getUnitName())
                .unitPrice(item.getUnitPrice())
                .weightKg(item.getWeightKg())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .isActive(item.getIsActive())
                .build();
    }
}
