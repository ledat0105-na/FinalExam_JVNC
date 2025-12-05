package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PromotionItems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(PromotionItemId.class)
public class PromotionItem {
    @Id
    @ManyToOne
    @JoinColumn(name = "PromotionId")
    private Promotion promotion;

    @Id
    @ManyToOne
    @JoinColumn(name = "ItemId")
    private Item item;
}

