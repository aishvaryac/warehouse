package com.warehouseslot.service;

import com.warehouseslot.dto.StockRequest;
import com.warehouseslot.entity.Bin;
import com.warehouseslot.entity.Product;
import com.warehouseslot.entity.Stock;
import com.warehouseslot.repository.BinRepository;
import com.warehouseslot.repository.ProductRepository;
import com.warehouseslot.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final BinRepository binRepository;

    public StockService(
            StockRepository stockRepository,
            ProductRepository productRepository,
            BinRepository binRepository) {

        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.binRepository = binRepository;
    }

    // Receive incoming stock and automatically assign a bin
    public Stock receiveStock(StockRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found with id: "
                                        + request.getProductId()
                        ));

        if (request.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        List<Bin> bins =
                binRepository.findAllByOrderByDistanceFromDispatchAsc();

        Bin selectedBin = findSuitableBin(product, request.getQuantity(), bins);

        if (selectedBin == null) {
            throw new RuntimeException(
                    "No suitable bin available for this stock"
            );
        }

        double newQuantity =
                selectedBin.getCurrentQuantity()
                        + request.getQuantity();

        selectedBin.setCurrentQuantity(newQuantity);

        binRepository.save(selectedBin);

        Stock stock = new Stock(
                product,
                selectedBin,
                request.getQuantity()
        );

        return stockRepository.save(stock);
    }

    // Find bin according to product velocity
    private Bin findSuitableBin(
            Product product,
            double quantity,
            List<Bin> bins) {

        String velocity = product.getVelocity();

        for (Bin bin : bins) {

            double availableCapacity =
                    bin.getCapacity()
                            - bin.getCurrentQuantity();

            if (availableCapacity < quantity) {
                continue;
            }

            // FAST products should be close to dispatch
            if ("FAST".equalsIgnoreCase(velocity)
                    && bin.getDistanceFromDispatch() <= 20) {

                return bin;
            }

            // MEDIUM products can use medium-distance bins
            if ("MEDIUM".equalsIgnoreCase(velocity)
                    && bin.getDistanceFromDispatch() <= 50) {

                return bin;
            }

            // SLOW products can use any available bin
            if ("SLOW".equalsIgnoreCase(velocity)) {

                return bin;
            }
        }

        // Fallback: any bin with enough capacity
        for (Bin bin : bins) {

            double availableCapacity =
                    bin.getCapacity()
                            - bin.getCurrentQuantity();

            if (availableCapacity >= quantity) {
                return bin;
            }
        }

        return null;
    }

    // Get all stock
    public List<Stock> getAllStock() {
        return stockRepository.findAll();
    }

    // Get stock by ID
    public Stock getStockById(Long id) {

        return stockRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Stock not found with id: " + id
                        ));
    }

    // Delete stock
    public void deleteStock(Long id) {

        Stock stock = getStockById(id);

        Bin bin = stock.getBin();

        // Return the quantity to bin capacity
        double updatedQuantity =
                bin.getCurrentQuantity()
                        - stock.getQuantity();

        bin.setCurrentQuantity(
                Math.max(updatedQuantity, 0)
        );

        binRepository.save(bin);

        stockRepository.delete(stock);
    }
}