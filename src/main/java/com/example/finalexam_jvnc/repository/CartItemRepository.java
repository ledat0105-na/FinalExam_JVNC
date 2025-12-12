package com.example.finalexam_jvnc.repository;

import com.example.finalexam_jvnc.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_CartId(Long cartId);

    Optional<CartItem> findByCart_CartIdAndItem_ItemId(Long cartId, Long itemId);

    void deleteByCart_CartId(Long cartId);
}
