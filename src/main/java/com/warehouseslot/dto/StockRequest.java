package com.warehouseslot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;


    @Positive(message = "Quantity must be greater than zero")
    private double quantity;


    public StockRequest() {
    }


    public StockRequest(
            Long productId,
            double quantity) {

        this.productId = productId;
        this.quantity = quantity;
    }


    public Long getProductId() {
        return productId;
    }


    public void setProductId(Long productId) {
        this.productId = productId;
    }


    public double getQuantity() {
        return quantity;
    }


    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}