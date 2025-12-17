package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.WalletTransactionDTO;
import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Wallet;
import com.example.finalexam_jvnc.model.WalletTransaction;
import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.WalletRepository;
import com.example.finalexam_jvnc.repository.WalletTransactionRepository;
import com.example.finalexam_jvnc.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Override
    public Double getBalance(String username) {
        return walletRepository.findByAccount_Username(username)
                .map(Wallet::getBalance)
                .orElse(0.0);
    }

    @Override
    public void deposit(String username, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Wallet wallet = walletRepository.findByAccount_Username(username)
                .orElseGet(() -> {
                    Account account = accountRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("Account not found"));
                    return Wallet.builder()
                            .account(account)
                            .balance(0.0)
                            .build();
                });

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
    }

    @Override
    public List<WalletTransactionDTO> getTransactionHistory(String username) {
        Wallet wallet = walletRepository.findByAccount_Username(username)
                .orElse(null);
        
        if (wallet == null) {
            return List.of();
        }

        List<WalletTransaction> transactions = walletTransactionRepository
                .findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId());

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private WalletTransactionDTO convertToDTO(WalletTransaction transaction) {
        String orderNumber = null;
        if (transaction.getPayment() != null && transaction.getPayment().getOrder() != null) {
            orderNumber = transaction.getPayment().getOrder().getOrderNumber();
        }

        return WalletTransactionDTO.builder()
                .walletTransactionId(transaction.getWalletTransactionId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .orderNumber(orderNumber)
                .build();
    }
}
