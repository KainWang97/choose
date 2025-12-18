package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.dto.response.ProductVariantDTO;
import com.choose.model.Product;
import com.choose.model.ProductVariant;
import com.choose.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final ProductService productService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductVariantDTO>>> getVariantsByProduct(@PathVariable Long productId) {
        List<ProductVariant> variants = productService.getProductVariants(productId);
        return ResponseEntity.ok(ApiResponse.success(ProductVariantDTO.fromEntities(variants)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> createVariant(@Valid @RequestBody VariantRequest request) {
        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setStock(request.getStock() != null ? request.getStock() : 0);
        
        ProductVariant created = productService.createProductVariant(variant);
        return ResponseEntity.ok(ApiResponse.success("Variant created successfully", ProductVariantDTO.fromEntity(created)));
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody VariantUpdateRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setColor(request.getColor());
        variant.setSize(request.getSize());
        variant.setStock(request.getStock());
        variant.setSkuCode(request.getSkuCode());
        
        ProductVariant updated = productService.updateProductVariant(variantId, variant);
        return ResponseEntity.ok(ApiResponse.success("Variant updated successfully", ProductVariantDTO.fromEntity(updated)));
    }

    @PatchMapping("/{variantId}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> updateStock(
            @PathVariable Long variantId,
            @Valid @RequestBody StockUpdateRequest request) {
        ProductVariant updated = productService.updateStock(variantId, request.getStock());
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", ProductVariantDTO.fromEntity(updated)));
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long variantId) {
        productService.deleteProductVariant(variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant deleted successfully", null));
    }

    @Data
    static class VariantRequest {
        @NotNull
        private Long productId;
        
        @NotBlank
        private String color;
        
        @NotBlank
        private String size;
        
        private Integer stock = 0;
    }

    @Data
    static class VariantUpdateRequest {
        private String color;
        private String size;
        private Integer stock;
        private String skuCode;
    }

    @Data
    static class StockUpdateRequest {
        @NotNull
        private Integer stock;
    }
}

