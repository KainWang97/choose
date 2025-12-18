package com.choose.dto.response;

import com.choose.model.Order;
import com.choose.model.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order DTO for frontend compatibility
 */
@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private BigDecimal total;
    private String status;
    private String paymentMethod;
    private String shippingMethod;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String paymentNote;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getOrderId());
        dto.setTotal(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setShippingMethod(order.getShippingMethod());
        dto.setRecipientName(order.getRecipientName());
        dto.setRecipientPhone(order.getRecipientPhone());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPaymentNote(order.getPaymentNote());
        dto.setCreatedAt(order.getCreatedAt());
        
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getUserId());
            dto.setUserEmail(order.getUser().getEmail());
            dto.setUserName(order.getUser().getName());
        }
        
        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .map(OrderItemDTO::fromEntity)
                    .toList());
        }
        
        return dto;
    }

    public static List<OrderDTO> fromEntities(List<Order> orders) {
        return orders.stream()
                .map(OrderDTO::fromEntity)
                .toList();
    }

    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long variantId;
        private String skuCode;
        private String productName;
        private String color;
        private String size;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public static OrderItemDTO fromEntity(OrderItem item) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setId(item.getOrderItemId());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getQuantity());
            dto.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            if (item.getVariant() != null) {
                dto.setVariantId(item.getVariant().getVariantId());
                dto.setSkuCode(item.getVariant().getSkuCode());
                dto.setColor(item.getVariant().getColor());
                dto.setSize(item.getVariant().getSize());
                
                if (item.getVariant().getProduct() != null) {
                    dto.setProductName(item.getVariant().getProduct().getName());
                }
            }
            
            return dto;
        }
    }
}

