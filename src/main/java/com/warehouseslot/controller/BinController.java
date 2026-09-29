package com.warehouseslot.controller;

import com.warehouseslot.entity.Bin;
import com.warehouseslot.service.BinService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bins")
@CrossOrigin(origins = "*")
public class BinController {

    private final BinService binService;

    public BinController(BinService binService) {
        this.binService = binService;
    }

    // ==============================
    // GET ALL BINS
    // ==============================
    @GetMapping
    public ResponseEntity<List<Bin>> getAllBins() {

        List<Bin> bins = binService.getAllBins();

        return ResponseEntity.ok(bins);
    }

    // ==============================
    // GET BIN BY ID
    // ==============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getBinById(@PathVariable Long id) {

        try {

            Bin bin = binService.getBinById(id);

            return ResponseEntity.ok(bin);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==============================
    // CREATE BIN
    // ==============================
    @PostMapping
    public ResponseEntity<?> createBin(@RequestBody Bin bin) {

        try {

            Bin savedBin = binService.saveBin(bin);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedBin);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==============================
    // UPDATE BIN
    // ==============================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBin(
            @PathVariable Long id,
            @RequestBody Bin bin) {

        try {

            Bin updatedBin = binService.updateBin(id, bin);

            return ResponseEntity.ok(updatedBin);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // ==============================
    // DELETE BIN
    // ==============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBin(@PathVariable Long id) {

        try {

            binService.deleteBin(id);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Bin deleted successfully."
                    )
            );

        } catch (IllegalStateException e) {

            // Bin is used by Pick List
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Unable to delete bin."
                    ));
        }
    }
}