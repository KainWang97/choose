package com.choose.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties(value = {"product", "hibernateLazyInitializer", "handler"})
@Table(name = "product_variants", 
       uniqueConstraints = @UniqueConstraint(name = "uk_product_color_size", columnNames = {"product_id", "color", "size"}),
       indexes = {
           @Index(name = "idx_stock", columnList = "stock")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, unique = true, length = 50)
    private String skuCode;

    @Column(name = "color", nullable = false, length = 20)
    private String color;

    @Column(name = "size", nullable = false, length = 10)
    private String size;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 提供 productId 給 JSON 序列化，避免 product 被忽略時無法取得關聯
    public Long getProductId() {
        return product != null ? product.getProductId() : null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

