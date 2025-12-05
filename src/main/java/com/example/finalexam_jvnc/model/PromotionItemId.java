package com.example.finalexam_jvnc.model;

import lombok.*;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PromotionItemId implements Serializable {
    private Long promotion;
    private Long item;
}
