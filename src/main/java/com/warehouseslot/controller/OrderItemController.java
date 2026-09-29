package com.warehouseslot.controller;

import com.warehouseslot.entity.OrderItem;
import com.warehouseslot.service.OrderItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "*")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    // ADD PRODUCT TO ORDER
    @PostMapping
    public ResponseEntity<OrderItem> addOrderItem(
            @RequestParam Long orderId,
            @RequestParam Long productId,
            @RequestParam double quantity) {

        OrderItem item = orderItemService.addOrderItem(
                orderId,
                productId,
                quantity
        );

        return new ResponseEntity<>(
                item,
                HttpStatus.CREATED
        );
    }

    // GET ITEMS OF AN ORDER
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderItemService.getItemsByOrder(orderId)
        );
    }

    // DELETE ORDER ITEM
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable Long id) {

        orderItemService.deleteOrderItem(id);

        return ResponseEntity.noContent().build();
    }
}