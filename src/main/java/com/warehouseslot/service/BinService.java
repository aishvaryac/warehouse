package com.warehouseslot.service;

import com.warehouseslot.entity.Bin;
import com.warehouseslot.repository.BinRepository;
import com.warehouseslot.repository.PickListItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BinService {

    private final BinRepository binRepository;
    private final PickListItemRepository pickListItemRepository;

    public BinService(
            BinRepository binRepository,
            PickListItemRepository pickListItemRepository) {

        this.binRepository = binRepository;
        this.pickListItemRepository = pickListItemRepository;
    }

    // GET ALL BINS
    public List<Bin> getAllBins() {
        return binRepository.findAll();
    }

    // GET BIN BY ID
    public Bin getBinById(Long id) {

        return binRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bin not found with ID: " + id
                        )
                );
    }

    // CREATE BIN
    public Bin saveBin(Bin bin) {
        return binRepository.save(bin);
    }

    // UPDATE BIN
    public Bin updateBin(Long id, Bin updatedBin) {

        Bin existingBin = binRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bin not found with ID: " + id
                        )
                );

        /*
         * Do not copy fields here yet because
         * your Bin entity field names are different.
         *
         * Saving the updated object keeps this service
         * compatible with your existing Bin entity.
         */

        updatedBin.setId(id);

        return binRepository.save(updatedBin);
    }

    // DELETE BIN
    public void deleteBin(Long id) {

        // Check whether bin exists
        if (!binRepository.existsById(id)) {

            throw new RuntimeException(
                    "Bin not found with ID: " + id
            );
        }

        // Check whether bin is being used in Pick List
        boolean usedInPickList =
                pickListItemRepository.existsByBinId(id);

        if (usedInPickList) {

            throw new IllegalStateException(
                    "Cannot delete Bin " + id +
                            " because it is currently used in a Pick List."
            );
        }

        // Delete only when it is not referenced
        binRepository.deleteById(id);
    }
}