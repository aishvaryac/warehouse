package com.warehouseslot.repository;

import com.warehouseslot.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Check whether a product is used in stock
    boolean existsByProductId(Long productId);

    // Find all stock records for a product
    List<Stock> findByProductId(Long productId);
}