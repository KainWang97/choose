package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.dto.response.CartItemDTO;
import com.choose.model.CartItem;
import com.choose.model.User;
import com.choose.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        List<CartItem> cartItems = cartService.getUserCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(CartItemDTO.fromEntities(cartItems)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        CartItem cartItem = cartService.addToCart(user.getUserId(), request.getVariantId(), request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Added to cart", CartItemDTO.fromEntity(cartItem)));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        CartItem updated = cartService.updateQuantity(cartItemId, request.getQuantity(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Quantity updated", CartItemDTO.fromEntity(updated)));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        cartService.removeFromCart(cartItemId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Removed from cart", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    @Data
    static class AddToCartRequest {
        @NotNull
        private Long variantId;
        
        @NotNull
        @Positive
        private Integer quantity = 1;
    }

    @Data
    static class UpdateQuantityRequest {
        @NotNull
        @Positive
        private Integer quantity;
    }
}
