package com.choose.repository;

import com.choose.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductProductId(Long productId);
    Optional<ProductVariant> findBySkuCode(String skuCode);
    Optional<ProductVariant> findByProductProductIdAndColorIgnoreCaseAndSizeIgnoreCase(Long productId, String color, String size);
}

