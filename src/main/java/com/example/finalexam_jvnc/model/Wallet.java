package com.example.finalexam_jvnc.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne
    @JoinColumn(name = "AccountId", nullable = false, unique = true)
    private Account account;

    private Double balance = 0.0;
}

