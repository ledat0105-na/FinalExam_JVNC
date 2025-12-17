package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.roles ORDER BY a.createdAt DESC")
    List<Account> findAllWithRoles();
    
    List<Account> findByIsLocked(Boolean isLocked);
    List<Account> findByIsActive(Boolean isActive);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.lastLoginAt >= :startOfDay AND a.lastLoginAt < :endOfDay")
    Long countAccountsLoggedInToday(LocalDateTime startOfDay, LocalDateTime endOfDay);
}

