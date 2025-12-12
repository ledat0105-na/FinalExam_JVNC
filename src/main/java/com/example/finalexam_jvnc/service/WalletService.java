package com.example.finalexam_jvnc.service;

public interface WalletService {
    Double getBalance(String username);

    void deposit(String username, Double amount);
}
