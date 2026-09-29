package com.warehouseslot.controller;

import com.warehouseslot.entity.Order;
import com.warehouseslot.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    // CREATE ORDER
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order) {

        Order savedOrder =
                orderService.createOrder(order);

        return new ResponseEntity<>(
                savedOrder,
                HttpStatus.CREATED
        );
    }


    // GET ALL ORDERS
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }


    // GET ORDER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrderById(id)
        );
    }


    // EDIT ORDER
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order) {

        return ResponseEntity.ok(
                orderService.updateOrder(
                        id,
                        order
                )
        );
    }


    // UPDATE ONLY STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                orderService.updateStatus(
                        id,
                        status
                )
        );
    }


    // DELETE ORDER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity.noContent()
                .build();
    }

}