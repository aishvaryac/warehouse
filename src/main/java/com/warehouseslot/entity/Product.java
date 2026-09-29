package com.warehouseslot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;


    @NotBlank(message = "SKU is required")
    @Column(nullable = false, unique = true)
    private String sku;


    @NotBlank(message = "Velocity is required")
    @Pattern(
            regexp = "FAST|MEDIUM|SLOW",
            message = "Velocity must be FAST, MEDIUM or SLOW"
    )
    @Column(nullable = false)
    private String velocity;


    public Product() {
    }


    public Product(
            String name,
            String sku,
            String velocity) {

        this.name = name;
        this.sku = sku;
        this.velocity = velocity;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public String getVelocity() {
        return velocity;
    }

    public void setVelocity(String velocity) {
        this.velocity = velocity;
    }
}