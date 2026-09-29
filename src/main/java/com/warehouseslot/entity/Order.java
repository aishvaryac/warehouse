package com.warehouseslot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Order number is required")
    @Column(nullable = false, unique = true)
    private String orderNumber;


    @Column(nullable = false)
    private String status;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    public Order() {
    }


    public Order(
            String orderNumber,
            String status) {

        this.orderNumber = orderNumber;

        this.status =
                status == null || status.isBlank()
                        ? "PENDING"
                        : status;

        this.createdAt =
                LocalDateTime.now();
    }


    @PrePersist
    public void beforeCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getOrderNumber() {
        return orderNumber;
    }


    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}