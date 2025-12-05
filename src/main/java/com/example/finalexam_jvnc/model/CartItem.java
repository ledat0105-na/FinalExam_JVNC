package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CartItems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "CartId", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "ItemId", nullable = false)
    private Item item;

    private Integer quantity;
    private Double unitPrice;
    private Double discountAmount;
}

