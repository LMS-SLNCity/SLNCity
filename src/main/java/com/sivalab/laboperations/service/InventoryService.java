package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.InventoryItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing inventory items
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private InventoryTransactionService transactionService;

    /**
     * Create new inventory item
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCreateItem")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryItem createInventoryItem(InventoryItem item) {
        logger.info("Creating new inventory item: {}", item.getName());
        
        // Check if SKU already exists
        if (inventoryRepository.findBySku(item.getSku()).isPresent()) {
            throw new IllegalArgumentException("Item with SKU " + item.getSku() + " already exists");
        }
        
        // Check if barcode already exists (if provided)
        if (item.getBarcode() != null && inventoryRepository.findByBarcode(item.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("Item with barcode " + item.getBarcode() + " already exists");
        }
        
        return inventoryRepository.save(item);
    }

    /**
     * Get inventory item by ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItem")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<InventoryItem> getInventoryItemById(Long id) {
        logger.debug("Fetching inventory item with ID: {}", id);
        return inventoryRepository.findById(id);
    }

    /**
     * Get inventory item by SKU
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItemBySku")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<InventoryItem> getInventoryItemBySku(String sku) {
        logger.debug("Fetching inventory item with SKU: {}", sku);
        return inventoryRepository.findBySku(sku);
    }

    /**
     * Get inventory item by barcode
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItemByBarcode")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<InventoryItem> getInventoryItemByBarcode(String barcode) {
        logger.debug("Fetching inventory item with barcode: {}", barcode);
        return inventoryRepository.findByBarcode(barcode);
    }

    /**
     * Get all inventory items
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetAllItems")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getAllInventoryItems() {
        logger.debug("Fetching all inventory items");
        return inventoryRepository.findAll();
    }

    /**
     * Get items by category
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItemsByCategory")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getItemsByCategory(InventoryCategory category) {
        logger.debug("Fetching items with category: {}", category);
        return inventoryRepository.findByCategory(category);
    }

    /**
     * Search inventory items with pagination
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackSearchItems")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Page<InventoryItem> searchInventoryItems(String name, String sku, InventoryCategory category, 
                                                  InventoryStatus status, String supplier, Pageable pageable) {
        logger.debug("Searching inventory items with criteria - name: {}, sku: {}, category: {}, status: {}, supplier: {}", 
                    name, sku, category, status, supplier);
        return inventoryRepository.searchItems(name, sku, category, status, supplier, pageable);
    }

    /**
     * Update inventory item
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateItem")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryItem updateInventoryItem(Long id, InventoryItem updatedItem) {
        logger.info("Updating inventory item with ID: {}", id);
        
        return inventoryRepository.findById(id)
                .map(item -> {
                    item.setName(updatedItem.getName());
                    item.setDescription(updatedItem.getDescription());
                    item.setCategory(updatedItem.getCategory());
                    item.setUnitOfMeasurement(updatedItem.getUnitOfMeasurement());
                    item.setMinimumStockLevel(updatedItem.getMinimumStockLevel());
                    item.setMaximumStockLevel(updatedItem.getMaximumStockLevel());
                    item.setReorderPoint(updatedItem.getReorderPoint());
                    item.setUnitCost(updatedItem.getUnitCost());
                    item.setSupplier(updatedItem.getSupplier());
                    item.setSupplierCatalogNumber(updatedItem.getSupplierCatalogNumber());
                    item.setStorageLocation(updatedItem.getStorageLocation());
                    item.setStorageConditions(updatedItem.getStorageConditions());
                    item.setExpiryDate(updatedItem.getExpiryDate());
                    item.setLotNumber(updatedItem.getLotNumber());
                    item.setStatus(updatedItem.getStatus());
                    item.setNotes(updatedItem.getNotes());
                    return inventoryRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found with ID: " + id));
    }

    /**
     * Update stock level
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateStock")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryItem updateStockLevel(Long id, Integer newStock, InventoryTransaction.TransactionType transactionType, 
                                        String reason, String performedBy) {
        logger.info("Updating stock level for item ID: {} to {}", id, newStock);
        
        return inventoryRepository.findById(id)
                .map(item -> {
                    Integer oldStock = item.getCurrentStock();
                    Integer quantity = Math.abs(newStock - oldStock);
                    
                    // Create transaction record
                    InventoryTransaction transaction = new InventoryTransaction();
                    transaction.setInventoryItem(item);
                    transaction.setTransactionType(transactionType);
                    transaction.setQuantity(quantity);
                    transaction.setStockBefore(oldStock);
                    transaction.setStockAfter(newStock);
                    transaction.setReason(reason);
                    transaction.setPerformedBy(performedBy);
                    
                    transactionService.createTransaction(transaction);
                    
                    // Update stock level
                    item.setCurrentStock(newStock);
                    return inventoryRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found with ID: " + id));
    }

    /**
     * Add stock (stock in)
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackAddStock")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryItem addStock(Long id, Integer quantity, BigDecimal unitCost, String supplier, 
                                String lotNumber, LocalDateTime expiryDate, String performedBy) {
        logger.info("Adding {} units to inventory item ID: {}", quantity, id);
        
        return inventoryRepository.findById(id)
                .map(item -> {
                    Integer oldStock = item.getCurrentStock();
                    Integer newStock = oldStock + quantity;
                    
                    // Create transaction record
                    InventoryTransaction transaction = new InventoryTransaction();
                    transaction.setInventoryItem(item);
                    transaction.setTransactionType(InventoryTransaction.TransactionType.STOCK_IN);
                    transaction.setQuantity(quantity);
                    transaction.setUnitCost(unitCost);
                    transaction.setTotalCost(unitCost != null ? unitCost.multiply(BigDecimal.valueOf(quantity)) : null);
                    transaction.setSupplier(supplier);
                    transaction.setLotNumber(lotNumber);
                    transaction.setExpiryDate(expiryDate);
                    transaction.setStockBefore(oldStock);
                    transaction.setStockAfter(newStock);
                    transaction.setPerformedBy(performedBy);
                    
                    transactionService.createTransaction(transaction);
                    
                    // Update item
                    item.setCurrentStock(newStock);
                    if (unitCost != null) {
                        item.setUnitCost(unitCost);
                    }
                    if (supplier != null) {
                        item.setSupplier(supplier);
                    }
                    if (lotNumber != null) {
                        item.setLotNumber(lotNumber);
                    }
                    if (expiryDate != null) {
                        item.setExpiryDate(expiryDate);
                    }
                    
                    return inventoryRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found with ID: " + id));
    }

    /**
     * Remove stock (stock out)
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackRemoveStock")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryItem removeStock(Long id, Integer quantity, String reason, String performedBy) {
        logger.info("Removing {} units from inventory item ID: {}", quantity, id);
        
        return inventoryRepository.findById(id)
                .map(item -> {
                    Integer oldStock = item.getCurrentStock();
                    
                    if (oldStock < quantity) {
                        throw new IllegalArgumentException("Insufficient stock. Available: " + oldStock + ", Requested: " + quantity);
                    }
                    
                    Integer newStock = oldStock - quantity;
                    
                    // Create transaction record
                    InventoryTransaction transaction = new InventoryTransaction();
                    transaction.setInventoryItem(item);
                    transaction.setTransactionType(InventoryTransaction.TransactionType.STOCK_OUT);
                    transaction.setQuantity(quantity);
                    transaction.setStockBefore(oldStock);
                    transaction.setStockAfter(newStock);
                    transaction.setReason(reason);
                    transaction.setPerformedBy(performedBy);
                    
                    transactionService.createTransaction(transaction);
                    
                    // Update stock level
                    item.setCurrentStock(newStock);
                    return inventoryRepository.save(item);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found with ID: " + id));
    }

    /**
     * Get low stock items
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetLowStockItems")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getLowStockItems() {
        logger.debug("Fetching low stock items");
        return inventoryRepository.findLowStockItems();
    }

    /**
     * Get items expiring soon
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItemsExpiringSoon")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getItemsExpiringSoon(int daysThreshold) {
        logger.debug("Fetching items expiring within {} days", daysThreshold);
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        return inventoryRepository.findItemsExpiringSoon(thresholdDate);
    }

    /**
     * Get expired items
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetExpiredItems")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getExpiredItems() {
        logger.debug("Fetching expired items");
        return inventoryRepository.findExpiredItems(LocalDateTime.now());
    }

    /**
     * Get items requiring reorder
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetItemsRequiringReorder")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryItem> getItemsRequiringReorder() {
        logger.debug("Fetching items requiring reorder");
        return inventoryRepository.findItemsRequiringReorder();
    }

    /**
     * Get inventory statistics
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetInventoryStatistics")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Map<String, Object> getInventoryStatistics() {
        logger.debug("Generating inventory statistics");

        List<Object[]> categoryStats = inventoryRepository.getInventoryStatisticsByCategory();
        List<Object[]> statusStats = inventoryRepository.getInventoryStatusStatistics();
        List<Object[]> valueStats = inventoryRepository.getInventoryValueByCategory();

        Map<InventoryCategory, Map<String, Object>> categoryMap = categoryStats.stream()
                .collect(Collectors.toMap(
                        row -> (InventoryCategory) row[0],
                        row -> Map.of("count", row[1], "totalStock", row[2])
                ));

        Map<InventoryStatus, Long> statusMap = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (InventoryStatus) row[0],
                        row -> (Long) row[1]
                ));

        Map<InventoryCategory, BigDecimal> valueMap = valueStats.stream()
                .collect(Collectors.toMap(
                        row -> (InventoryCategory) row[0],
                        row -> (BigDecimal) row[1]
                ));

        Double totalValue = inventoryRepository.getTotalInventoryValue();

        return Map.of(
                "categoryStatistics", categoryMap,
                "statusStatistics", statusMap,
                "valueByCategory", valueMap,
                "totalValue", totalValue != null ? totalValue : 0.0,
                "totalItems", inventoryRepository.count(),
                "lowStockItems", inventoryRepository.countLowStockItems(),
                "itemsExpiringSoon", inventoryRepository.countItemsExpiringSoon(LocalDateTime.now().plusDays(30))
        );
    }

    // Fallback methods
    public InventoryItem fallbackCreateItem(InventoryItem item, Exception ex) {
        logger.error("Fallback: Failed to create inventory item", ex);
        throw new RuntimeException("Inventory service temporarily unavailable");
    }

    public Optional<InventoryItem> fallbackGetItem(Long id, Exception ex) {
        logger.error("Fallback: Failed to get inventory item by ID", ex);
        return Optional.empty();
    }

    public Optional<InventoryItem> fallbackGetItemBySku(String sku, Exception ex) {
        logger.error("Fallback: Failed to get inventory item by SKU", ex);
        return Optional.empty();
    }

    public Optional<InventoryItem> fallbackGetItemByBarcode(String barcode, Exception ex) {
        logger.error("Fallback: Failed to get inventory item by barcode", ex);
        return Optional.empty();
    }

    public List<InventoryItem> fallbackGetAllItems(Exception ex) {
        logger.error("Fallback: Failed to get all inventory items", ex);
        return List.of();
    }

    public List<InventoryItem> fallbackGetItemsByCategory(InventoryCategory category, Exception ex) {
        logger.error("Fallback: Failed to get items by category", ex);
        return List.of();
    }

    public Page<InventoryItem> fallbackSearchItems(String name, String sku, InventoryCategory category,
                                                  InventoryStatus status, String supplier, Pageable pageable, Exception ex) {
        logger.error("Fallback: Failed to search inventory items", ex);
        return Page.empty();
    }

    public InventoryItem fallbackUpdateItem(Long id, InventoryItem updatedItem, Exception ex) {
        logger.error("Fallback: Failed to update inventory item", ex);
        throw new RuntimeException("Inventory service temporarily unavailable");
    }

    public InventoryItem fallbackUpdateStock(Long id, Integer newStock, InventoryTransaction.TransactionType transactionType,
                                           String reason, String performedBy, Exception ex) {
        logger.error("Fallback: Failed to update stock level", ex);
        throw new RuntimeException("Inventory service temporarily unavailable");
    }

    public InventoryItem fallbackAddStock(Long id, Integer quantity, BigDecimal unitCost, String supplier,
                                        String lotNumber, LocalDateTime expiryDate, String performedBy, Exception ex) {
        logger.error("Fallback: Failed to add stock", ex);
        throw new RuntimeException("Inventory service temporarily unavailable");
    }

    public InventoryItem fallbackRemoveStock(Long id, Integer quantity, String reason, String performedBy, Exception ex) {
        logger.error("Fallback: Failed to remove stock", ex);
        throw new RuntimeException("Inventory service temporarily unavailable");
    }

    public List<InventoryItem> fallbackGetLowStockItems(Exception ex) {
        logger.error("Fallback: Failed to get low stock items", ex);
        return List.of();
    }

    public List<InventoryItem> fallbackGetItemsExpiringSoon(int daysThreshold, Exception ex) {
        logger.error("Fallback: Failed to get items expiring soon", ex);
        return List.of();
    }

    public List<InventoryItem> fallbackGetExpiredItems(Exception ex) {
        logger.error("Fallback: Failed to get expired items", ex);
        return List.of();
    }

    public List<InventoryItem> fallbackGetItemsRequiringReorder(Exception ex) {
        logger.error("Fallback: Failed to get items requiring reorder", ex);
        return List.of();
    }

    public Map<String, Object> fallbackGetInventoryStatistics(Exception ex) {
        logger.error("Fallback: Failed to get inventory statistics", ex);
        return Map.of("error", "Statistics temporarily unavailable");
    }
}
