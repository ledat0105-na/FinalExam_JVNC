package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWallet_WalletIdOrderByCreatedAtDesc(Long walletId);
}

