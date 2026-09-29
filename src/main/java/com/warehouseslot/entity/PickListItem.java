package com.warehouseslot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pick_list_items")
public class PickListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pick_list_id", nullable = false)
    private PickList pickList;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "bin_id", nullable = false)
    private Bin bin;

    @Column(nullable = false)
    private double quantity;

    @Column(name = "pick_sequence", nullable = false)
    private int pickSequence;

    @Column(nullable = false)
    private double distance;

    public PickListItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PickList getPickList() {
        return pickList;
    }

    public void setPickList(PickList pickList) {
        this.pickList = pickList;
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

    public int getPickSequence() {
        return pickSequence;
    }

    public void setPickSequence(int pickSequence) {
        this.pickSequence = pickSequence;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}