package com.warehouseslot.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "bins")
public class Bin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Bin code is required")
    @Column(nullable = false, unique = true)
    private String binCode;


    @NotBlank(message = "Zone is required")
    @Column(nullable = false)
    private String zone;


    @Min(
            value = 0,
            message = "Distance cannot be negative"
    )
    @Column(nullable = false)
    private double distanceFromDispatch;


    @Min(
            value = 1,
            message = "Capacity must be greater than zero"
    )
    @Column(nullable = false)
    private double capacity;


    @Min(
            value = 0,
            message = "Current quantity cannot be negative"
    )
    @Column(nullable = false)
    private double currentQuantity;


    public Bin() {
    }


    public Bin(
            String binCode,
            String zone,
            double distanceFromDispatch,
            double capacity,
            double currentQuantity) {

        this.binCode = binCode;
        this.zone = zone;
        this.distanceFromDispatch = distanceFromDispatch;
        this.capacity = capacity;
        this.currentQuantity = currentQuantity;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }


    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }


    public double getDistanceFromDispatch() {
        return distanceFromDispatch;
    }

    public void setDistanceFromDispatch(
            double distanceFromDispatch) {

        this.distanceFromDispatch =
                distanceFromDispatch;
    }


    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }


    public double getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(
            double currentQuantity) {

        this.currentQuantity =
                currentQuantity;
    }
}