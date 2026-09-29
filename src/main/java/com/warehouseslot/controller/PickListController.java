package com.warehouseslot.controller;

import com.warehouseslot.entity.PickList;
import com.warehouseslot.entity.PickListItem;
import com.warehouseslot.service.PickListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pick-lists")
@CrossOrigin(origins = "*")
public class PickListController {

    private final PickListService pickListService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PickListController(PickListService pickListService) {
        this.pickListService = pickListService;
    }


    // =========================================================
    // GET ALL PICK LISTS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getAllPickLists() {

        try {

            List<PickList> pickLists =
                    pickListService.getAllPickLists();

            return ResponseEntity.ok(pickLists);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not load pick lists",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // GET ONE PICK LIST
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getPickList(
            @PathVariable Long id
    ) {

        try {

            PickList pickList =
                    pickListService.getPickListById(id);

            if (pickList == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error",
                                "Pick List not found"
                        ));
            }

            return ResponseEntity.ok(pickList);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not load pick list",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // GENERATE PICK LIST
    // =========================================================

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<?> generatePickList(
            @PathVariable Long orderId
    ) {

        try {

            PickList existing =
                    pickListService.findByOrderId(orderId);

            /*
             * Don't create duplicate Pick Lists.
             */
            if (existing != null) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error",
                                "Pick List already exists for this order",
                                "id",
                                existing.getId()
                        ));
            }


            PickList pickList =
                    pickListService.generatePickList(orderId);


            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(pickList);

        } catch (IllegalArgumentException e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Invalid request",
                            "message",
                            getSafeMessage(e)
                    ));

        } catch (IllegalStateException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error",
                            "Cannot generate pick list",
                            "message",
                            getSafeMessage(e)
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not generate pick list",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // GET PICK LIST ITEMS
    // =========================================================

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getPickListItems(
            @PathVariable Long id
    ) {

        try {

            PickList pickList =
                    pickListService.getPickListById(id);

            if (pickList == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error",
                                "Pick List not found"
                        ));
            }


            List<PickListItem> items =
                    pickListService.getPickListItems(id);


            if (items == null) {
                items = Collections.emptyList();
            }


            return ResponseEntity.ok(items);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not load pick list items",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // COMPLETE PICK LIST
    // =========================================================

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completePickList(
            @PathVariable Long id
    ) {

        try {

            PickList pickList =
                    pickListService.completePickList(id);


            return ResponseEntity.ok(pickList);

        } catch (IllegalArgumentException e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Invalid Pick List",
                            "message",
                            getSafeMessage(e)
                    ));

        } catch (IllegalStateException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error",
                            "Cannot complete Pick List",
                            "message",
                            getSafeMessage(e)
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not complete pick list",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // DELETE PICK LIST
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePickList(
            @PathVariable Long id
    ) {

        try {

            pickListService.deletePickList(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Pick List deleted successfully"
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error",
                            getSafeMessage(e)
                    ));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            "Could not delete pick list",
                            "message",
                            getSafeMessage(e)
                    ));
        }
    }


    // =========================================================
    // SAFE ERROR MESSAGE
    // =========================================================

    private String getSafeMessage(Exception e) {

        if (e == null) {
            return "Unknown server error";
        }

        if (e.getMessage() == null ||
                e.getMessage().isBlank()) {

            return e.getClass().getSimpleName();
        }

        return e.getMessage();
    }
}