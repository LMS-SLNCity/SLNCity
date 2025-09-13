package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing inventory items
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Management", description = "Inventory management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Create new inventory item
     */
    @PostMapping
    @Operation(summary = "Create new inventory item", description = "Add new item to inventory")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem item) {
        InventoryItem createdItem = inventoryService.createInventoryItem(item);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    /**
     * Get inventory item by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get inventory item by ID", description = "Retrieve inventory item details by ID")
    public ResponseEntity<InventoryItem> getInventoryItemById(
            @Parameter(description = "Inventory item ID") @PathVariable Long id) {
        return inventoryService.getInventoryItemById(id)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get inventory item by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get inventory item by SKU", description = "Retrieve inventory item details by SKU")
    public ResponseEntity<InventoryItem> getInventoryItemBySku(
            @Parameter(description = "Item SKU") @PathVariable String sku) {
        return inventoryService.getInventoryItemBySku(sku)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get inventory item by barcode
     */
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get inventory item by barcode", description = "Retrieve inventory item details by barcode")
    public ResponseEntity<InventoryItem> getInventoryItemByBarcode(
            @Parameter(description = "Item barcode") @PathVariable String barcode) {
        return inventoryService.getInventoryItemByBarcode(barcode)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all inventory items
     */
    @GetMapping
    @Operation(summary = "Get all inventory items", description = "Retrieve all inventory items")
    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get items by category", description = "Retrieve inventory items filtered by category")
    public ResponseEntity<List<InventoryItem>> getItemsByCategory(
            @Parameter(description = "Inventory category") @PathVariable InventoryCategory category) {
        List<InventoryItem> items = inventoryService.getItemsByCategory(category);
        return ResponseEntity.ok(items);
    }

    /**
     * Search inventory items with pagination
     */
    @GetMapping("/search")
    @Operation(summary = "Search inventory items", description = "Search inventory items with multiple criteria and pagination")
    public ResponseEntity<Page<InventoryItem>> searchInventoryItems(
            @Parameter(description = "Item name") @RequestParam(required = false) String name,
            @Parameter(description = "SKU") @RequestParam(required = false) String sku,
            @Parameter(description = "Category") @RequestParam(required = false) InventoryCategory category,
            @Parameter(description = "Status") @RequestParam(required = false) InventoryStatus status,
            @Parameter(description = "Supplier") @RequestParam(required = false) String supplier,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InventoryItem> items = inventoryService.searchInventoryItems(
                name, sku, category, status, supplier, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Update inventory item
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update inventory item", description = "Update existing inventory item details")
    public ResponseEntity<InventoryItem> updateInventoryItem(
            @Parameter(description = "Inventory item ID") @PathVariable Long id,
            @Valid @RequestBody InventoryItem item) {
        try {
            InventoryItem updatedItem = inventoryService.updateInventoryItem(id, item);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add stock to inventory item
     */
    @PostMapping("/{id}/add-stock")
    @Operation(summary = "Add stock", description = "Add stock to inventory item")
    public ResponseEntity<InventoryItem> addStock(
            @Parameter(description = "Inventory item ID") @PathVariable Long id,
            @Parameter(description = "Quantity to add") @RequestParam Integer quantity,
            @Parameter(description = "Unit cost") @RequestParam(required = false) BigDecimal unitCost,
            @Parameter(description = "Supplier") @RequestParam(required = false) String supplier,
            @Parameter(description = "Lot number") @RequestParam(required = false) String lotNumber,
            @Parameter(description = "Expiry date") @RequestParam(required = false) String expiryDate,
            @Parameter(description = "Performed by") @RequestParam String performedBy) {
        try {
            LocalDateTime expiry = expiryDate != null ? LocalDateTime.parse(expiryDate) : null;
            InventoryItem updatedItem = inventoryService.addStock(id, quantity, unitCost, supplier, lotNumber, expiry, performedBy);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove stock from inventory item
     */
    @PostMapping("/{id}/remove-stock")
    @Operation(summary = "Remove stock", description = "Remove stock from inventory item")
    public ResponseEntity<InventoryItem> removeStock(
            @Parameter(description = "Inventory item ID") @PathVariable Long id,
            @Parameter(description = "Quantity to remove") @RequestParam Integer quantity,
            @Parameter(description = "Reason for removal") @RequestParam String reason,
            @Parameter(description = "Performed by") @RequestParam String performedBy) {
        try {
            InventoryItem updatedItem = inventoryService.removeStock(id, quantity, reason, performedBy);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Update stock level
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update stock level", description = "Update inventory item stock level")
    public ResponseEntity<InventoryItem> updateStockLevel(
            @Parameter(description = "Inventory item ID") @PathVariable Long id,
            @Parameter(description = "New stock level") @RequestParam Integer newStock,
            @Parameter(description = "Transaction type") @RequestParam InventoryTransaction.TransactionType transactionType,
            @Parameter(description = "Reason") @RequestParam String reason,
            @Parameter(description = "Performed by") @RequestParam String performedBy) {
        try {
            InventoryItem updatedItem = inventoryService.updateStockLevel(id, newStock, transactionType, reason, performedBy);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get low stock items
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", description = "Retrieve items with low stock levels")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        List<InventoryItem> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get items expiring soon
     */
    @GetMapping("/expiring-soon")
    @Operation(summary = "Get items expiring soon", description = "Retrieve items expiring within specified days")
    public ResponseEntity<List<InventoryItem>> getItemsExpiringSoon(
            @Parameter(description = "Days threshold") @RequestParam(defaultValue = "30") int daysThreshold) {
        List<InventoryItem> items = inventoryService.getItemsExpiringSoon(daysThreshold);
        return ResponseEntity.ok(items);
    }

    /**
     * Get expired items
     */
    @GetMapping("/expired")
    @Operation(summary = "Get expired items", description = "Retrieve expired inventory items")
    public ResponseEntity<List<InventoryItem>> getExpiredItems() {
        List<InventoryItem> items = inventoryService.getExpiredItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get items requiring reorder
     */
    @GetMapping("/reorder-required")
    @Operation(summary = "Get items requiring reorder", description = "Retrieve items that need to be reordered")
    public ResponseEntity<List<InventoryItem>> getItemsRequiringReorder() {
        List<InventoryItem> items = inventoryService.getItemsRequiringReorder();
        return ResponseEntity.ok(items);
    }

    /**
     * Get inventory statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get inventory statistics", description = "Retrieve inventory statistics and metrics")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        Map<String, Object> statistics = inventoryService.getInventoryStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get inventory categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get inventory categories", description = "Retrieve all available inventory categories")
    public ResponseEntity<InventoryCategory[]> getInventoryCategories() {
        return ResponseEntity.ok(InventoryCategory.values());
    }

    /**
     * Get inventory statuses
     */
    @GetMapping("/statuses")
    @Operation(summary = "Get inventory statuses", description = "Retrieve all available inventory statuses")
    public ResponseEntity<InventoryStatus[]> getInventoryStatuses() {
        return ResponseEntity.ok(InventoryStatus.values());
    }

    /**
     * Get transaction types
     */
    @GetMapping("/transaction-types")
    @Operation(summary = "Get transaction types", description = "Retrieve all available transaction types")
    public ResponseEntity<InventoryTransaction.TransactionType[]> getTransactionTypes() {
        return ResponseEntity.ok(InventoryTransaction.TransactionType.values());
    }
}
