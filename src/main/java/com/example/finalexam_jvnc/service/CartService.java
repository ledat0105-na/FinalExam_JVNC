package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.AddToCartDTO;

public interface CartService {
    void addToCart(String username, AddToCartDTO addToCartDTO);

    void removeFromCart(String username, Long cartItemId);

    void updateCartItemQuantity(String username, Long cartItemId, Integer quantity);

    void clearCart(String username);

    Integer getCartItemCount(String username);

    com.example.finalexam_jvnc.dto.CartDTO getCart(String username);

    void applyCoupon(String username, String code);

    void removeCoupon(String username);
}
