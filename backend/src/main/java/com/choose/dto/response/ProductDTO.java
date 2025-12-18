package com.choose.dto.response;

import com.choose.model.Product;
import com.choose.model.ProductVariant;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product DTO for frontend compatibility
 * Maps backend naming to frontend expected format:
 * - productId -> id
 * - imageUrl -> image
 * - category object -> category name string
 * - variants stock -> aggregated stock
 */
@Data
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String category;
    private Long categoryId;
    private String image;
    private String description;
    private Boolean isListed;
    private Boolean isFeatured;
    private Integer stock;
    private LocalDateTime createdAt;

    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImageUrl());
        dto.setIsListed(product.getIsListed());
        dto.setIsFeatured(product.getIsFeatured() != null ? product.getIsFeatured() : false);
        dto.setCreatedAt(product.getCreatedAt());
        
        // Map category
        if (product.getCategory() != null) {
            dto.setCategory(product.getCategory().getName());
            dto.setCategoryId(product.getCategory().getCategoryId());
        }
        
        // Aggregate stock from variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            int totalStock = product.getVariants().stream()
                    .mapToInt(ProductVariant::getStock)
                    .sum();
            dto.setStock(totalStock);
        } else {
            dto.setStock(0);
        }
        
        return dto;
    }

    public static List<ProductDTO> fromEntities(List<Product> products) {
        return products.stream()
                .map(ProductDTO::fromEntity)
                .toList();
    }
}

