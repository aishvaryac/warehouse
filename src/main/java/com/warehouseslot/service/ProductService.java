package com.warehouseslot.service;

import com.warehouseslot.entity.Product;
import com.warehouseslot.repository.ProductRepository;
import com.warehouseslot.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public ProductService(
            ProductRepository productRepository,
            StockRepository stockRepository) {

        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    // ============================
    // GET ALL PRODUCTS
    // ============================

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ============================
    // GET PRODUCT BY ID
    // ============================

    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with ID: " + id
                        )
                );
    }

    // ============================
    // CREATE PRODUCT
    // ============================

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // ============================
    // UPDATE PRODUCT
    // ============================

    public Product updateProduct(
            Long id,
            Product updatedProduct) {

        Product existingProduct =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found with ID: " + id
                                )
                        );

        /*
         * Keep the ID of the existing product.
         * Copy the fields that your Product entity has.
         */

        updatedProduct.setId(id);

        return productRepository.save(updatedProduct);
    }

    // ============================
    // DELETE PRODUCT
    // ============================

    public void deleteProduct(Long id) {

        // 1. Check product exists
        if (!productRepository.existsById(id)) {

            throw new RuntimeException(
                    "Product not found with ID: " + id
            );
        }

        // 2. Check whether product is used in STOCK
        boolean usedInStock =
                stockRepository.existsByProductId(id);

        if (usedInStock) {

            throw new IllegalStateException(
                    "Cannot delete Product " + id +
                            " because it is currently used in Stock."
            );
        }

        // 3. Delete only when it is not referenced
        productRepository.deleteById(id);
    }
}