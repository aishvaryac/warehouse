package com.warehouseslot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bin_id", nullable = false)
    private Bin bin;

    @Column(nullable = false)
    private double quantity;

    public Stock() {
    }

    public Stock(Product product, Bin bin, double quantity) {
        this.product = product;
        this.bin = bin;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}