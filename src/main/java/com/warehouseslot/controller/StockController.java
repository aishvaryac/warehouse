package com.warehouseslot.controller;

import com.warehouseslot.dto.StockRequest;
import com.warehouseslot.entity.Stock;
import com.warehouseslot.service.StockService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    private final StockService stockService;


    public StockController(StockService stockService) {
        this.stockService = stockService;
    }


    @PostMapping("/receive")
    public ResponseEntity<Stock> receiveStock(
            @Valid @RequestBody StockRequest request) {

        Stock stock =
                stockService.receiveStock(request);

        return new ResponseEntity<>(
                stock,
                HttpStatus.CREATED
        );
    }


    @GetMapping
    public ResponseEntity<List<Stock>> getAllStock() {

        return ResponseEntity.ok(
                stockService.getAllStock()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stockService.getStockById(id)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long id) {

        stockService.deleteStock(id);

        return ResponseEntity.noContent().build();
    }
}