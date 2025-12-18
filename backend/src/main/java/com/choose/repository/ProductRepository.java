package com.choose.repository;

import com.choose.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsListedTrue();
    List<Product> findByCategoryCategoryIdAndIsListedTrue(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.isListed = true AND " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword% " +
           "OR p.category.name LIKE %:keyword% OR p.category.description LIKE %:keyword%)")
    List<Product> searchProducts(@Param("keyword") String keyword);

    // Featured Products
    List<Product> findByIsFeaturedTrueAndIsListedTrueOrderByCreatedAtDesc();
    
    long countByIsFeaturedTrue();
}

