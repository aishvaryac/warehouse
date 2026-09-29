package com.warehouseslot.service;

import com.warehouseslot.entity.Bin;
import com.warehouseslot.entity.Order;
import com.warehouseslot.entity.OrderItem;
import com.warehouseslot.entity.PickList;
import com.warehouseslot.entity.PickListItem;
import com.warehouseslot.entity.Product;
import com.warehouseslot.entity.Stock;
import com.warehouseslot.repository.BinRepository;
import com.warehouseslot.repository.OrderItemRepository;
import com.warehouseslot.repository.OrderRepository;
import com.warehouseslot.repository.PickListItemRepository;
import com.warehouseslot.repository.PickListRepository;
import com.warehouseslot.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PickListService {

    private final PickListRepository pickListRepository;
    private final PickListItemRepository pickListItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockRepository stockRepository;
    private final BinRepository binRepository;

    public PickListService(
            PickListRepository pickListRepository,
            PickListItemRepository pickListItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            StockRepository stockRepository,
            BinRepository binRepository
    ) {
        this.pickListRepository = pickListRepository;
        this.pickListItemRepository = pickListItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.stockRepository = stockRepository;
        this.binRepository = binRepository;
    }

    // =========================================================
    // GET ALL PICK LISTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PickList> getAllPickLists() {
        return pickListRepository.findAll();
    }

    // =========================================================
    // GET PICK LIST BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public PickList getPickListById(Long id) {

        if (id == null) {
            return null;
        }

        Optional<PickList> result =
                pickListRepository.findById(id);

        return result.orElse(null);
    }

    // =========================================================
    // FIND PICK LIST BY ORDER ID
    // =========================================================

    @Transactional(readOnly = true)
    public PickList findByOrderId(Long orderId) {

        if (orderId == null) {
            return null;
        }

        return pickListRepository
                .findByOrderId(orderId)
                .orElse(null);
    }

    // =========================================================
    // GENERATE PICK LIST
    // =========================================================

    @Transactional
    public PickList generatePickList(Long orderId) {

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Order ID is required"
            );
        }

        // Find order
        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        // Check whether pick list already exists
        PickList existing =
                findByOrderId(orderId);

        if (existing != null) {
            throw new IllegalStateException(
                    "Pick List already exists for order "
                            + orderId
            );
        }

        // Get order items
        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(orderId);

        if (orderItems == null ||
                orderItems.isEmpty()) {

            throw new IllegalStateException(
                    "Order "
                            + order.getOrderNumber()
                            + " has no order items"
            );
        }

        // Create pick list
        PickList pickList = new PickList();

        pickList.setOrder(order);
        pickList.setStatus("READY");

        pickList =
                pickListRepository.save(pickList);

        int sequence = 1;

        // =====================================================
        // PROCESS EACH ORDER ITEM
        // =====================================================

        for (OrderItem orderItem : orderItems) {

            if (orderItem == null) {
                continue;
            }

            Product product =
                    orderItem.getProduct();

            if (product == null) {

                throw new IllegalStateException(
                        "Order item "
                                + orderItem.getId()
                                + " has no product"
                );
            }

            double requiredQuantity =
                    orderItem.getQuantity();

            if (requiredQuantity <= 0) {

                throw new IllegalStateException(
                        "Invalid quantity for product "
                                + product.getId()
                );
            }

            // Get stock for product
            List<Stock> availableStock =
                    stockRepository.findByProductId(
                            product.getId()
                    );

            if (availableStock == null ||
                    availableStock.isEmpty()) {

                throw new IllegalStateException(
                        "No stock available for product: "
                                + product.getName()
                );
            }

            // =================================================
            // FILTER AND SORT STOCK BY BIN DISTANCE
            // =================================================

            availableStock =
                    availableStock.stream()
                            .filter(stock ->
                                    stock != null
                                            && stock.getQuantity() > 0
                                            && stock.getBin() != null
                            )
                            .sorted(
                                    Comparator.comparing(
                                            stock ->
                                                    getBinDistance(
                                                            stock.getBin()
                                                    )
                                    )
                            )
                            .toList();

            if (availableStock.isEmpty()) {

                throw new IllegalStateException(
                        "No usable stock found for product: "
                                + product.getName()
                );
            }

            double remaining =
                    requiredQuantity;

            // =================================================
            // ALLOCATE STOCK FROM NEAREST BINS
            // =================================================

            for (Stock stock : availableStock) {

                if (remaining <= 0) {
                    break;
                }

                double available =
                        stock.getQuantity();

                double picked =
                        Math.min(
                                available,
                                remaining
                        );

                if (picked <= 0) {
                    continue;
                }

                Bin bin =
                        stock.getBin();

                // =================================================
                // CREATE PICK LIST ITEM
                // =================================================

                PickListItem item =
                        new PickListItem();

                item.setPickList(pickList);

                item.setProduct(product);

                item.setBin(bin);

                item.setQuantity(picked);

                item.setPickSequence(sequence++);

                /*
                 * IMPORTANT:
                 *
                 * distance is required in the database.
                 * Therefore we must set it before saving.
                 */
                item.setDistance(
                        getBinDistance(bin)
                );

                pickListItemRepository.save(item);

                remaining -= picked;
            }

            // =================================================
            // CHECK WHETHER ENOUGH STOCK WAS FOUND
            // =================================================

            if (remaining > 0) {

                throw new IllegalStateException(
                        "Insufficient stock for product: "
                                + product.getName()
                                + ". Required: "
                                + requiredQuantity
                                + ", Missing: "
                                + remaining
                );
            }
        }

        // Return saved pick list
        return pickListRepository
                .findById(pickList.getId())
                .orElse(pickList);
    }

    // =========================================================
    // GET PICK LIST ITEMS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PickListItem> getPickListItems(
            Long pickListId
    ) {

        if (pickListId == null) {
            return List.of();
        }

        return pickListItemRepository
                .findByPickListIdOrderByPickSequenceAsc(
                        pickListId
                );
    }

    // =========================================================
    // COMPLETE PICK LIST
    // =========================================================

    @Transactional
    public PickList completePickList(
            Long pickListId
    ) {

        if (pickListId == null) {

            throw new IllegalArgumentException(
                    "Pick List ID is required"
            );
        }

        // Find pick list
        PickList pickList =
                pickListRepository.findById(pickListId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pick List not found: "
                                                + pickListId
                                )
                        );

        // Check status
        if ("COMPLETED".equalsIgnoreCase(
                pickList.getStatus()
        )) {

            throw new IllegalStateException(
                    "Pick List is already completed"
            );
        }

        // Get pick list items
        List<PickListItem> items =
                pickListItemRepository
                        .findByPickListIdOrderByPickSequenceAsc(
                                pickListId
                        );

        if (items == null ||
                items.isEmpty()) {

            throw new IllegalStateException(
                    "Pick List has no items"
            );
        }

        // =====================================================
        // PROCESS EACH PICK LIST ITEM
        // =====================================================

        for (PickListItem item : items) {

            if (item == null) {
                continue;
            }

            Product product =
                    item.getProduct();

            Bin bin =
                    item.getBin();

            double quantityToRemove =
                    item.getQuantity();

            if (product == null) {

                throw new IllegalStateException(
                        "Pick List item has no product"
                );
            }

            if (bin == null) {

                throw new IllegalStateException(
                        "Pick List item has no bin"
                );
            }

            if (quantityToRemove <= 0) {
                continue;
            }

            // Find product stock
            List<Stock> stocks =
                    stockRepository.findByProductId(
                            product.getId()
                    );

            if (stocks == null ||
                    stocks.isEmpty()) {

                throw new IllegalStateException(
                        "Stock not found for product "
                                + product.getName()
                );
            }

            // =================================================
            // FIND STOCK IN THE CORRECT BIN
            // =================================================

            Stock matchingStock =
                    stocks.stream()
                            .filter(stock ->
                                    stock != null
                                            && stock.getBin() != null
                                            && stock.getBin().getId() != null
                                            && stock.getBin()
                                            .getId()
                                            .equals(
                                                    bin.getId()
                                            )
                            )
                            .findFirst()
                            .orElse(null);

            if (matchingStock == null) {

                throw new IllegalStateException(
                        "Stock not found for product "
                                + product.getName()
                                + " in bin "
                                + bin.getBinCode()
                );
            }

            // =================================================
            // CHECK STOCK QUANTITY
            // =================================================

            if (matchingStock.getQuantity()
                    < quantityToRemove) {

                throw new IllegalStateException(
                        "Insufficient stock in bin "
                                + bin.getBinCode()
                                + " for product "
                                + product.getName()
                );
            }

            // =================================================
            // REDUCE STOCK
            // =================================================

            double newStockQuantity =
                    matchingStock.getQuantity()
                            - quantityToRemove;

            matchingStock.setQuantity(
                    newStockQuantity
            );

            stockRepository.save(
                    matchingStock
            );

            // =================================================
            // REDUCE BIN QUANTITY
            // =================================================

            double currentBinQuantity =
                    bin.getCurrentQuantity();

            double newBinQuantity =
                    Math.max(
                            0,
                            currentBinQuantity
                                    - quantityToRemove
                    );

            bin.setCurrentQuantity(
                    newBinQuantity
            );

            binRepository.save(bin);
        }

        // =====================================================
        // MARK PICK LIST COMPLETED
        // =====================================================

        pickList.setStatus("COMPLETED");

        pickList =
                pickListRepository.save(
                        pickList
                );

        // =====================================================
        // MARK ORDER COMPLETED
        // =====================================================

        Order order =
                pickList.getOrder();

        if (order != null) {

            order.setStatus("COMPLETED");

            orderRepository.save(order);
        }

        return pickList;
    }

    // =========================================================
    // DELETE PICK LIST
    // =========================================================

    @Transactional
    public void deletePickList(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Pick List ID is required"
            );
        }

        PickList pickList =
                pickListRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pick List not found: "
                                                + id
                                )
                        );

        // Delete child items first
        List<PickListItem> items =
                pickListItemRepository
                        .findByPickListIdOrderByPickSequenceAsc(
                                id
                        );

        if (items != null &&
                !items.isEmpty()) {

            pickListItemRepository.deleteAll(items);
        }

        // Delete parent
        pickListRepository.delete(pickList);
    }

    // =========================================================
    // GET BIN DISTANCE
    // =========================================================

    private double getBinDistance(Bin bin) {

        if (bin == null) {
            return Double.MAX_VALUE;
        }

        /*
         * distanceFromDispatch is a primitive double.
         * Do NOT compare it with null.
         */
        return bin.getDistanceFromDispatch();
    }
}