package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "StockItems", uniqueConstraints = @UniqueConstraint(columnNames = {"WarehouseId", "ItemId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockItemId;

    @ManyToOne
    @JoinColumn(name = "WarehouseId", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "ItemId", nullable = false)
    private Item item;

    private Integer quantityOnHand = 0;
    private Integer quantityReserved = 0;
    private Integer lowStockThreshold = 0;
}

