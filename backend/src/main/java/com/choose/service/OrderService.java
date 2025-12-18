package com.choose.service;

import com.choose.model.*;
import com.choose.repository.OrderRepository;
import com.choose.repository.ProductVariantRepository;
import com.choose.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserUserId(userId);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public Order createOrder(Long userId, Order order) {
        log.info("Creating order for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Order creation failed: User not found, userId={}", userId);
                    return new IllegalArgumentException("User not found");
                });
        
        order.setUser(user);
        
        // Calculate total amount and validate stock
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariant().getVariantId())
                    .orElseThrow(() -> {
                        log.error("Order creation failed: Product variant not found, variantId={}", 
                                item.getVariant().getVariantId());
                        return new IllegalArgumentException("Product variant not found");
                    });
            
            // Check stock availability
            if (variant.getStock() < item.getQuantity()) {
                log.warn("Insufficient stock for order: userId={}, variantId={}, sku={}, requested={}, available={}", 
                        userId, variant.getVariantId(), variant.getSkuCode(), item.getQuantity(), variant.getStock());
                throw new IllegalArgumentException("Insufficient stock for variant: " + variant.getSkuCode());
            }
            
            // Set price from variant's product
            item.setPrice(variant.getProduct().getPrice());
            item.setVariant(variant);
            item.setOrder(order);
            
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            // Update stock
            int oldStock = variant.getStock();
            variant.setStock(variant.getStock() - item.getQuantity());
            productVariantRepository.save(variant);
            log.debug("Stock updated: variantId={}, oldStock={}, newStock={}", 
                    variant.getVariantId(), oldStock, variant.getStock());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created successfully: orderId={}, userId={}, totalAmount={}", 
                savedOrder.getOrderId(), userId, totalAmount);
        
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order status: orderId={}, newStatus={}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order status update failed: Order not found, orderId={}", orderId);
                    return new IllegalArgumentException("Order not found");
                });
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        
        log.info("Order status updated: orderId={}, oldStatus={}, newStatus={}", orderId, oldStatus, status);
        return saved;
    }

    @Transactional
    public Order updatePaymentNote(Long orderId, String paymentNote) {
        log.info("Updating payment note: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setPaymentNote(paymentNote);
        return orderRepository.save(order);
    }

    /**
     * 檢查用戶是否有進行中的訂單（待付款、已付款待出貨、運送中）
     */
    public boolean hasActiveOrders(Long userId) {
        java.util.List<Order.OrderStatus> activeStatuses = java.util.Arrays.asList(
            Order.OrderStatus.PENDING,
            Order.OrderStatus.PAID,
            Order.OrderStatus.SHIPPED
        );
        return orderRepository.existsByUserUserIdAndStatusIn(userId, activeStatuses);
    }
}
