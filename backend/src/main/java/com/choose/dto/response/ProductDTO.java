package com.choose.dto.response;

import com.choose.model.Product;
import com.choose.model.ProductVariant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product DTO for frontend compatibility
 * Maps backend naming to frontend expected format:
 * - productId -> id
 * - imageUrl -> image
 * - category object -> category name string
 * - variants stock -> aggregated stock
 * - colorImages JSON -> colorImages Map
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
    private Map<String, List<String>> colorImages; // 顏色對應圖片 Map

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        
        // Parse colorImages JSON
        if (product.getColorImages() != null && !product.getColorImages().isEmpty()) {
            try {
                dto.setColorImages(objectMapper.readValue(
                    product.getColorImages(), 
                    new TypeReference<Map<String, List<String>>>() {}
                ));
            } catch (Exception e) {
                dto.setColorImages(null);
            }
        }
        
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


