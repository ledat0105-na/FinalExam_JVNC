package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer_UsernameAndStatus(String username, String status);

    Optional<Cart> findByCustomer_AccountIdAndStatus(Long accountId, String status);
}
