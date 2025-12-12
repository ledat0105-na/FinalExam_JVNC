package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findBySku(String sku);

    List<Item> findByIsActiveTrue();

    List<Item> findByCategoryCategoryId(Long categoryId);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.category")
    List<Item> findAllWithCategory();

    @Query("SELECT i FROM Item i WHERE i.isActive = true " +
            "AND (:keyword IS NULL OR i.itemName LIKE CONCAT('%', :keyword, '%') OR i.description LIKE CONCAT('%', :keyword, '%')) "
            +
            "AND (:categoryId IS NULL OR i.category.categoryId = :categoryId)")
    List<Item> searchActiveItems(String keyword, Long categoryId);
}
