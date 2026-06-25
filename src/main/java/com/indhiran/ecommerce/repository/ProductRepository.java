package com.indhiran.ecommerce.repository;

import com.indhiran.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // Search by name containing keyword (case insensitive)
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(
            String name, Pageable pageable);

    // Filter by category
    Page<Product> findByCategoryIdAndActiveTrue(
            Long categoryId, Pageable pageable);

    // Filter by price range
    Page<Product> findByPriceBetweenAndActiveTrue(
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Get all active products
    Page<Product> findByActiveTrue(Pageable pageable);

}