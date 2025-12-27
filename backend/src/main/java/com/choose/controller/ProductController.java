package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.dto.response.ProductDTO;
import com.choose.model.Category;
import com.choose.model.Product;
import com.choose.dto.response.ProductVariantDTO;
import com.choose.model.ProductVariant;
import com.choose.service.CategoryService;
import com.choose.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    // ========== Public Endpoints ==========

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<Product> products = productService.getAllListedProducts();
        List<ProductDTO> dtos = ProductDTO.fromEntities(products);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId)
                .map(product -> ResponseEntity.ok(ApiResponse.success(ProductDTO.fromEntity(product))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> dtos = ProductDTO.fromEntities(products);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        List<ProductDTO> dtos = ProductDTO.fromEntities(products);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantDTO>>> getProductVariants(@PathVariable Long productId) {
        List<ProductVariant> variants = productService.getProductVariants(productId);
        return ResponseEntity.ok(ApiResponse.success(ProductVariantDTO.fromEntities(variants)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getFeaturedProducts() {
        List<Product> products = productService.getFeaturedProducts();
        List<ProductDTO> dtos = ProductDTO.fromEntities(products);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    // ========== Admin Endpoints ==========

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProductsAdmin() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> dtos = ProductDTO.fromEntities(products);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductRequest request) {
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required for creating a product");
        }
        Category category = categoryService.getCategoryById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
        
        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setIsListed(request.getIsListed() != null ? request.getIsListed() : true);
        product.setColorImages(request.getColorImagesJson());
        
        Product created = productService.createProduct(product);
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", ProductDTO.fromEntity(created)));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        
        Product updatedData = new Product();
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
            updatedData.setCategory(category);
        }
        updatedData.setName(request.getName());
        updatedData.setDescription(request.getDescription());
        updatedData.setPrice(request.getPrice());
        updatedData.setImageUrl(request.getImageUrl());
        updatedData.setIsListed(request.getIsListed());
        updatedData.setColorImages(request.getColorImagesJson());
        
        Product updated = productService.updateProduct(productId, updatedData);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", ProductDTO.fromEntity(updated)));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @PatchMapping("/{productId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariant>> updateProductStock(
            @PathVariable Long productId,
            @RequestBody StockUpdateRequest request) {
        ProductVariant updated = productService.updateStock(request.getVariantId(), request.getStock());
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updated));
    }

    @PatchMapping("/{productId}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleFeatured(@PathVariable Long productId) {
        Product updated = productService.toggleFeatured(productId);
        return ResponseEntity.ok(ApiResponse.success(
            updated.getIsFeatured() ? "已加入新品上架" : "已從新品上架移除", 
            ProductDTO.fromEntity(updated)
        ));
    }

    // Request DTOs
    @Data
    static class ProductRequest {
        private Long categoryId;
        
        @NotBlank
        private String name;
        
        private String description;
        
        @NotNull
        @Positive
        private BigDecimal price;
        
        private String imageUrl;
        
        private Boolean isListed;
        
        private Map<String, List<String>> colorImages;
        
        // 將 Map 轉換為 JSON 字串
        public String getColorImagesJson() {
            if (colorImages == null || colorImages.isEmpty()) {
                return null;
            }
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(colorImages);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Data
    static class StockUpdateRequest {
        @NotNull
        private Long variantId;
        
        @NotNull
        private Integer stock;
    }
}
