package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Wallet;
import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.WalletRepository;
import com.example.finalexam_jvnc.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AccountRepository accountRepository;

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
}
