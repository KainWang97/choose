package com.choose.service;

import com.choose.model.CartItem;
import com.choose.model.ProductVariant;
import com.choose.model.User;
import com.choose.repository.CartItemRepository;
import com.choose.repository.ProductVariantRepository;
import com.choose.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    public List<CartItem> getUserCart(Long userId) {
        return cartItemRepository.findByUserUserId(userId);
    }

    public Optional<CartItem> getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long variantId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserUserIdAndVariantVariantId(userId, variantId);
        
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            
            // Check stock
            if (newQuantity > variant.getStock()) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + variant.getStock());
            }
            
            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        } else {
            // Check stock
            if (quantity > variant.getStock()) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + variant.getStock());
            }
            
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setVariant(variant);
            cartItem.setQuantity(quantity);
            return cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public CartItem updateQuantity(Long cartItemId, Integer quantity, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        // Ownership check
        if (!cartItem.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item does not belong to the user");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        // Check stock
        if (quantity > cartItem.getVariant().getStock()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + cartItem.getVariant().getStock());
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(Long cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        // Ownership check
        if (!cartItem.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item does not belong to the user");
        }

        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserUserId(userId);
    }
}

