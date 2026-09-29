package com.warehouseslot.controller;

import com.warehouseslot.entity.Product;
import com.warehouseslot.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    productService.getProductById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestBody Product product) {

        try {

            Product saved =
                    productService.saveProduct(product);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        try {

            Product updated =
                    productService.updateProduct(id, product);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id) {

        try {

            productService.deleteProduct(id);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Product deleted successfully."
                    )
            );

        } catch (IllegalStateException e) {

            // Product is being used by Stock
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message",
                            "Unable to delete product."
                    ));
        }
    }
}