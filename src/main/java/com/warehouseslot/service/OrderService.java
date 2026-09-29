package com.warehouseslot.service;

import com.warehouseslot.entity.Order;
import com.warehouseslot.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // CREATE
    public Order createOrder(Order order) {

        if (order.getOrderNumber() == null ||
                order.getOrderNumber().isBlank()) {

            throw new RuntimeException(
                    "Order number is required"
            );
        }

        if (order.getStatus() == null ||
                order.getStatus().isBlank()) {

            order.setStatus("PENDING");
        }

        if (order.getCreatedAt() == null) {
            order.setCreatedAt(
                    java.time.LocalDateTime.now()
            );
        }

        return orderRepository.save(order);
    }


    // GET ALL
    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }


    // GET BY ID
    public Order getOrderById(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );
    }


    // UPDATE ORDER
    public Order updateOrder(
            Long id,
            Order updatedOrder) {

        Order existingOrder =
                getOrderById(id);


        if (updatedOrder.getOrderNumber() == null ||
                updatedOrder.getOrderNumber().isBlank()) {

            throw new RuntimeException(
                    "Order number is required"
            );
        }


        existingOrder.setOrderNumber(
                updatedOrder.getOrderNumber().trim()
        );


        if (updatedOrder.getStatus() != null &&
                !updatedOrder.getStatus().isBlank()) {

            existingOrder.setStatus(
                    updatedOrder.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }


        return orderRepository.save(
                existingOrder
        );
    }


    // UPDATE STATUS
    public Order updateStatus(
            Long id,
            String status) {

        Order order =
                getOrderById(id);


        if (status == null ||
                status.isBlank()) {

            throw new RuntimeException(
                    "Status is required"
            );
        }


        order.setStatus(
                status.trim().toUpperCase()
        );


        return orderRepository.save(order);
    }


    // DELETE
    public void deleteOrder(Long id) {

        Order order =
                getOrderById(id);

        orderRepository.delete(order);
    }

}