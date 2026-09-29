package com.warehouseslot.service;

import com.warehouseslot.entity.Order;
import com.warehouseslot.entity.OrderItem;
import com.warehouseslot.entity.Product;
import com.warehouseslot.repository.OrderItemRepository;
import com.warehouseslot.repository.OrderRepository;
import com.warehouseslot.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemService(
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            ProductRepository productRepository) {

        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // Add product to an order
    public OrderItem addOrderItem(
            Long orderId,
            Long productId,
            double quantity) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + orderId
                        ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: " + productId
                        ));

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        OrderItem orderItem =
                new OrderItem(order, product, quantity);

        return orderItemRepository.save(orderItem);
    }

    // Get all items in an order
    public List<OrderItem> getItemsByOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + orderId
                        ));

        return orderItemRepository.findByOrder(order);
    }

    // Delete order item
    public void deleteOrderItem(Long id) {

        OrderItem item = orderItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order item not found with id: " + id
                        ));

        orderItemRepository.delete(item);
    }
}