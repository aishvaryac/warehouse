package com.warehouseslot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "pick_lists",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "order_id")
        }
)
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class PickList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * One PickList belongs to one Order.
     *
     * JsonIgnore prevents:
     *
     * PickList -> Order -> PickList -> Order -> ...
     *
     * which can cause invalid/infinite JSON responses.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({
            "pickList",
            "pickLists",
            "orderItems"
    })
    private Order order;

    @Column(nullable = false)
    private String status = "READY";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /*
     * PickListItems are returned separately through:
     *
     * GET /api/pick-lists/{id}/items
     *
     * Therefore don't serialize them inside the PickList JSON.
     */
    @OneToMany(
            mappedBy = "pickList",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    private List<PickListItem> items = new ArrayList<>();


    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PickList() {
    }


    public PickList(Order order) {
        this.order = order;
        this.status = "READY";
        this.createdAt = LocalDateTime.now();
    }


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "READY";
        }
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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


    public List<PickListItem> getItems() {
        return items;
    }

    public void setItems(List<PickListItem> items) {
        this.items = items;
    }


    // =========================================================
    // HELPER
    // =========================================================

    public void addItem(PickListItem item) {

        if (item == null) {
            return;
        }

        items.add(item);
        item.setPickList(this);
    }


    public void removeItem(PickListItem item) {

        if (item == null) {
            return;
        }

        items.remove(item);
        item.setPickList(null);
    }
}