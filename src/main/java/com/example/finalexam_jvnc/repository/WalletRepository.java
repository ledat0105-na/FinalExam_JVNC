package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccount_Username(String username);
}
