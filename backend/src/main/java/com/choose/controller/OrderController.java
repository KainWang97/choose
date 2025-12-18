package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.dto.response.OrderDTO;
import com.choose.model.Order;
import com.choose.model.OrderItem;
import com.choose.model.ProductVariant;
import com.choose.model.User;
import com.choose.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * Admin: Get all orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDTO> dtos = OrderDTO.fromEntities(orders);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Get current user's orders
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        List<Order> orders = orderService.getUserOrders(user.getUserId());
        List<OrderDTO> dtos = OrderDTO.fromEntities(orders);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Admin: Get orders by user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderDTO> dtos = OrderDTO.fromEntities(orders);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> ResponseEntity.ok(ApiResponse.success(OrderDTO.fromEntity(order))))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create order for current user (user from JWT)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        
        Order order = new Order();
        order.setShippingMethod(request.getShippingMethod());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setShippingAddress(request.getShippingAddress());
        
        // Convert items to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            ProductVariant variant = new ProductVariant();
            variant.setVariantId(itemRequest.getVariantId());
            item.setVariant(variant);
            item.setQuantity(itemRequest.getQuantity());
            orderItems.add(item);
        }
        order.setOrderItems(orderItems);
        
        Order createdOrder = orderService.createOrder(user.getUserId(), order);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", OrderDTO.fromEntity(createdOrder)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusUpdateRequest request) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated", OrderDTO.fromEntity(updatedOrder)));
    }

    @PatchMapping("/{orderId}/payment-note")
    public ResponseEntity<ApiResponse<OrderDTO>> updatePaymentNote(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @RequestBody PaymentNoteRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = order.getUser() != null
                && order.getUser().getUserId().equals(user.getUserId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Forbidden: not your order"));
        }

        Order updatedOrder = orderService.updatePaymentNote(orderId, request.getPaymentNote());
        return ResponseEntity.ok(ApiResponse.success("Payment note updated", OrderDTO.fromEntity(updatedOrder)));
    }

    // Request DTOs
    @Data
    static class CreateOrderRequest {
        @NotBlank
        private String shippingMethod;
        
        private String paymentMethod = "BANK_TRANSFER";
        
        @NotBlank
        private String recipientName;
        
        @NotBlank
        private String recipientPhone;
        
        @NotBlank
        private String shippingAddress;
        
        @NotEmpty
        private List<OrderItemRequest> items;
    }

    @Data
    static class OrderItemRequest {
        @NotNull
        private Long variantId;
        
        @NotNull
        @Positive
        private Integer quantity;
    }

    @Data
    static class StatusUpdateRequest {
        @NotNull
        private Order.OrderStatus status;
    }

    @Data
    static class PaymentNoteRequest {
        private String paymentNote;
    }
}
