package com.choose.dto.response;

import com.choose.model.ProductVariant;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private String skuCode;
    private String color;
    private String size;
    private Integer stock;
    private LocalDateTime createdAt;

    public static ProductVariantDTO fromEntity(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getVariantId());
        if (variant.getProduct() != null) {
            dto.setProductId(variant.getProduct().getProductId());
        }
        dto.setSkuCode(variant.getSkuCode());
        dto.setColor(variant.getColor());
        dto.setSize(variant.getSize());
        dto.setStock(variant.getStock());
        dto.setCreatedAt(variant.getCreatedAt());
        return dto;
    }

    public static List<ProductVariantDTO> fromEntities(List<ProductVariant> variants) {
        return variants.stream()
                .map(ProductVariantDTO::fromEntity)
                .toList();
    }
}

