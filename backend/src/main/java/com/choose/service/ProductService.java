package com.choose.service;

import com.choose.model.Product;
import com.choose.model.ProductVariant;
import com.choose.repository.ProductRepository;
import com.choose.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public List<Product> getAllListedProducts() {
        return productRepository.findByIsListedTrue();
    }

    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryCategoryIdAndIsListedTrue(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating product: name={}", product.getName());
        Product saved = productRepository.save(product);
        log.info("Product created: productId={}, name={}", saved.getProductId(), saved.getName());
        return saved;
    }

    @Transactional
    public Product updateProduct(Long productId, Product updatedProduct) {
        log.info("Updating product: productId={}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product update failed: Product not found, productId={}", productId);
                    return new IllegalArgumentException("Product not found");
                });
        
        if (updatedProduct.getName() != null) {
            product.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            product.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getPrice() != null) {
            product.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getImageUrl() != null) {
            product.setImageUrl(updatedProduct.getImageUrl());
        }
        if (updatedProduct.getIsListed() != null) {
            product.setIsListed(updatedProduct.getIsListed());
        }
        
        Product saved = productRepository.save(product);
        log.info("Product updated: productId={}", productId);
        return saved;
    }

    public List<ProductVariant> getProductVariants(Long productId) {
        return productVariantRepository.findByProductProductId(productId);
    }

    @Transactional
    public ProductVariant createProductVariant(ProductVariant variant) {
        log.info("Creating product variant: productId={}, sku={}",
                variant.getProduct().getProductId(), variant.getSkuCode());

        // Normalize color/size
        String color = variant.getColor() != null ? variant.getColor().trim() : "";
        String size = variant.getSize() != null ? variant.getSize().trim() : "";
        variant.setColor(color);
        variant.setSize(size);

        // 檢查同商品+顏色+尺寸是否已存在
        productVariantRepository.findByProductProductIdAndColorIgnoreCaseAndSizeIgnoreCase(
                variant.getProduct().getProductId(), color, size
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Variant already exists for the same color and size");
        });

        // 若未提供 SKU，自動產生且確保唯一
        if (variant.getSkuCode() == null || variant.getSkuCode().isBlank()) {
            String generated = generateUniqueSku(
                    variant.getProduct().getProductId(),
                    variant.getColor(),
                    variant.getSize()
            );
            variant.setSkuCode(generated);
        }

        ProductVariant saved = productVariantRepository.save(variant);
        log.info("Product variant created: variantId={}, sku={}", saved.getVariantId(), saved.getSkuCode());
        return saved;
    }

    /** 依 productId + color + size 產生唯一 SKU，若衝突則追加隨機碼再重試 */
    private String generateUniqueSku(Long productId, String color, String size) {
        String colorPart = color != null ? color.replaceAll("\\s+", "").toUpperCase() : "NA";
        String sizePart = size != null ? size.replaceAll("\\s+", "").toUpperCase() : "NA";

        // 簡單格式：P{productId}-{color}-{size}-{6碼隨機}
        for (int i = 0; i < 5; i++) {
            String candidate = String.format("P%s-%s-%s-%06d",
                    productId,
                    abbreviate(colorPart, 4),
                    abbreviate(sizePart, 4),
                    (int) (Math.random() * 1_000_000));

            if (productVariantRepository.findBySkuCode(candidate).isEmpty()) {
                return candidate;
            }
        }

        // 最後保險再用 UUID 片段
        String fallback = "P" + productId + "-" + System.currentTimeMillis();
        return fallback;
    }

    private String abbreviate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }

    @Transactional
    public ProductVariant updateStock(Long variantId, Integer stock) {
        log.info("Updating stock: variantId={}, newStock={}", variantId, stock);
        
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> {
                    log.error("Stock update failed: Variant not found, variantId={}", variantId);
                    return new IllegalArgumentException("Product variant not found");
                });
        
        if (stock < 0) {
            log.warn("Stock update rejected: Negative stock not allowed, variantId={}, attemptedStock={}", 
                    variantId, stock);
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        
        int oldStock = variant.getStock();
        variant.setStock(stock);
        ProductVariant saved = productVariantRepository.save(variant);
        
        log.info("Stock updated: variantId={}, sku={}, oldStock={}, newStock={}", 
                variantId, variant.getSkuCode(), oldStock, stock);
        return saved;
    }

    @Transactional
    public ProductVariant updateProductVariant(Long variantId, ProductVariant updatedVariant) {
        log.info("Updating product variant: variantId={}", variantId);
        
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> {
                    log.error("Variant update failed: Variant not found, variantId={}", variantId);
                    return new IllegalArgumentException("Product variant not found");
                });
        
        if (updatedVariant.getColor() != null) {
            variant.setColor(updatedVariant.getColor());
        }
        if (updatedVariant.getSize() != null) {
            variant.setSize(updatedVariant.getSize());
        }
        if (updatedVariant.getStock() != null) {
            if (updatedVariant.getStock() < 0) {
                throw new IllegalArgumentException("Stock cannot be negative");
            }
            variant.setStock(updatedVariant.getStock());
        }
        if (updatedVariant.getSkuCode() != null) {
            variant.setSkuCode(updatedVariant.getSkuCode());
        }
        
        ProductVariant saved = productVariantRepository.save(variant);
        log.info("Product variant updated: variantId={}, sku={}", variantId, saved.getSkuCode());
        return saved;
    }

    @Transactional
    public void deleteProductVariant(Long variantId) {
        log.info("Deleting product variant: variantId={}", variantId);
        
        if (!productVariantRepository.existsById(variantId)) {
            log.error("Variant deletion failed: Variant not found, variantId={}", variantId);
            throw new IllegalArgumentException("Product variant not found");
        }
        
        productVariantRepository.deleteById(variantId);
        log.info("Product variant deleted: variantId={}", variantId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: productId={}", productId);
        
        if (!productRepository.existsById(productId)) {
            log.error("Product deletion failed: Product not found, productId={}", productId);
            throw new IllegalArgumentException("Product not found");
        }
        
        productRepository.deleteById(productId);
        log.info("Product deleted: productId={}", productId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsListedTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public Product toggleFeatured(Long productId) {
        log.info("Toggling featured status: productId={}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Toggle featured failed: Product not found, productId={}", productId);
                    return new IllegalArgumentException("Product not found");
                });
        
        Boolean currentFeatured = product.getIsFeatured() != null && product.getIsFeatured();
        
        if (!currentFeatured) {
            // 檢查是否已達上限 5 個
            long featuredCount = productRepository.countByIsFeaturedTrue();
            if (featuredCount >= 5) {
                log.warn("Toggle featured rejected: Max 5 featured products, productId={}", productId);
                throw new IllegalArgumentException("新品上架最多 5 個商品");
            }
        }
        
        product.setIsFeatured(!currentFeatured);
        Product saved = productRepository.save(product);
        
        log.info("Featured status toggled: productId={}, isFeatured={}", productId, saved.getIsFeatured());
        return saved;
    }
}
