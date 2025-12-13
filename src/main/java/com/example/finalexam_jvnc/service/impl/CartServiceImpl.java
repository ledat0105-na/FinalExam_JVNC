package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.AddToCartDTO;
import com.example.finalexam_jvnc.model.Account;
import com.example.finalexam_jvnc.model.Cart;
import com.example.finalexam_jvnc.model.CartItem;
import com.example.finalexam_jvnc.model.Item;
import com.example.finalexam_jvnc.repository.AccountRepository;
import com.example.finalexam_jvnc.repository.CartItemRepository;
import com.example.finalexam_jvnc.repository.CartRepository;
import com.example.finalexam_jvnc.repository.ItemRepository;
import com.example.finalexam_jvnc.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void addToCart(String username, AddToCartDTO addToCartDTO) {
        Account customer = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setStatus("CART");
                    newCart.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        Item item = itemRepository.findById(addToCartDTO.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!Boolean.TRUE.equals(item.getIsActive())) {
            throw new RuntimeException("Item is not available");
        }

        CartItem cartItem = cartItemRepository.findByCart_CartIdAndItem_ItemId(cart.getCartId(), item.getItemId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + addToCartDTO.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setQuantity(addToCartDTO.getQuantity());
            cartItem.setUnitPrice(item.getUnitPrice());
            cartItem.setDiscountAmount(0.0); // Assume no discount for now
        }

        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeFromCart(String username, Long cartItemId) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Item does not belong to user's cart");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void updateCartItemQuantity(String username, Long cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new RuntimeException("Item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    public void clearCart(String username) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCart_CartId(cart.getCartId());
        }
    }

    @Override
    public Integer getCartItemCount(String username) {
        return cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .map(cart -> cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum())
                .orElse(0);
    }

    @Autowired
    private com.example.finalexam_jvnc.repository.PromotionRepository promotionRepository;

    @Override
    public com.example.finalexam_jvnc.dto.CartDTO getCart(String username) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElse(null);

        if (cart == null) {
            return com.example.finalexam_jvnc.dto.CartDTO.builder()
                    .items(java.util.Collections.emptyList())
                    .totalAmount(0.0)
                    .discountAmount(0.0)
                    .finalAmount(0.0)
                    .build();
        }

        java.util.List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());

        java.util.List<com.example.finalexam_jvnc.dto.CartItemDTO> itemDTOs = cartItems.stream()
                .map(item -> com.example.finalexam_jvnc.dto.CartItemDTO.builder()
                        .cartItemId(item.getCartItemId())
                        .itemId(item.getItem().getItemId())
                        .itemName(item.getItem().getItemName())
                        .sku(item.getItem().getSku())
                        .unitName(item.getItem().getUnitName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getUnitPrice() * item.getQuantity())
                        .imgUrl(item.getItem().getImageUrl())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        double totalAmount = itemDTOs.stream().mapToDouble(com.example.finalexam_jvnc.dto.CartItemDTO::getTotalPrice)
                .sum();

        double discountAmount = 0.0;
        if (cart.getPromotion() != null) {
            com.example.finalexam_jvnc.model.Promotion promo = cart.getPromotion();
            // Validate basic requirements again just in case (e.g. min order amount)
            if (promo.getMinOrderAmount() == null || totalAmount >= promo.getMinOrderAmount()) {
                if ("PERCENT".equals(promo.getDiscountType())) {
                    discountAmount = totalAmount * (promo.getDiscountValue() / 100);
                } else { // AMOUNT
                    discountAmount = promo.getDiscountValue();
                }
                // Logic to cap discount could be here
            }
        }

        if (discountAmount > totalAmount)
            discountAmount = totalAmount;

        return com.example.finalexam_jvnc.dto.CartDTO.builder()
                .cartId(cart.getCartId())
                .items(itemDTOs)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(totalAmount - discountAmount)
                .appliedCouponCode(cart.getPromotion() != null ? cart.getPromotion().getPromotionCode() : null)
                .build();
    }

    @Override
    public void applyCoupon(String username, String code) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        com.example.finalexam_jvnc.model.Promotion promotion = promotionRepository.findByPromotionCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        if (!Boolean.TRUE.equals(promotion.getIsActive())) {
            throw new RuntimeException("Coupon is not active");
        }

        if (promotion.getEndDate() != null && promotion.getEndDate().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        if (promotion.getStartDate() != null && promotion.getStartDate().isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Coupon is not yet active");
        }

        // Calculate current total
        double totalAmount = cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        if (promotion.getMinOrderAmount() != null && totalAmount < promotion.getMinOrderAmount()) {
            throw new RuntimeException(
                    "Minimum order amount not met for this coupon: " + promotion.getMinOrderAmount());
        }

        cart.setPromotion(promotion);
        cartRepository.save(cart);
    }

    @Override
    public void removeCoupon(String username) {
        Cart cart = cartRepository.findByCustomer_UsernameAndStatus(username, "CART")
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setPromotion(null);
        cartRepository.save(cart);
    }
}
