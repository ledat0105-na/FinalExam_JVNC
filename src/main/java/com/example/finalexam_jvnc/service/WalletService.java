package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.WalletTransactionDTO;

import java.util.List;

public interface WalletService {
    Double getBalance(String username);

    void deposit(String username, Double amount);

    List<WalletTransactionDTO> getTransactionHistory(String username);
}
