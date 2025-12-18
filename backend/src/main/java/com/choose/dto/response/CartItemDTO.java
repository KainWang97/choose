package com.choose.dto.response;

import com.choose.model.CartItem;
import com.choose.model.Product;
import com.choose.model.ProductVariant;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartItem DTO
 * 購物車項目資料傳輸物件，包含完整的商品和規格資訊
 */
@Data
public class CartItemDTO {
    private Long cartItemId;
    private Integer quantity;
    private VariantInfo variant;
    
    @Data
    public static class VariantInfo {
        private Long id;
        private String color;
        private String size;
        private Integer stock;
        private Long productId;
        private ProductInfo product;
    }
    
    @Data
    public static class ProductInfo {
        private Long id;
        private String name;
        private String image;
        private BigDecimal price;
    }
    
    public static CartItemDTO fromEntity(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setQuantity(cartItem.getQuantity());
        
        ProductVariant variant = cartItem.getVariant();
        if (variant != null) {
            VariantInfo variantInfo = new VariantInfo();
            variantInfo.setId(variant.getVariantId());
            variantInfo.setColor(variant.getColor());
            variantInfo.setSize(variant.getSize());
            variantInfo.setStock(variant.getStock());
            
            Product product = variant.getProduct();
            if (product != null) {
                variantInfo.setProductId(product.getProductId());
                
                ProductInfo productInfo = new ProductInfo();
                productInfo.setId(product.getProductId());
                productInfo.setName(product.getName());
                productInfo.setImage(product.getImageUrl());
                productInfo.setPrice(product.getPrice());
                variantInfo.setProduct(productInfo);
            }
            
            dto.setVariant(variantInfo);
        }
        
        return dto;
    }
    
    public static List<CartItemDTO> fromEntities(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItemDTO::fromEntity)
                .toList();
    }
}
