package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.InventoryCategory;
import com.sivalab.laboperations.entity.InventoryItem;
import com.sivalab.laboperations.entity.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InventoryItem entity
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Find inventory item by SKU
     */
    Optional<InventoryItem> findBySku(String sku);

    /**
     * Find inventory item by barcode
     */
    Optional<InventoryItem> findByBarcode(String barcode);

    /**
     * Find items by category
     */
    List<InventoryItem> findByCategory(InventoryCategory category);

    /**
     * Find items by status
     */
    List<InventoryItem> findByStatus(InventoryStatus status);

    /**
     * Find items by supplier
     */
    List<InventoryItem> findBySupplierContainingIgnoreCase(String supplier);

    /**
     * Find items by name containing (case insensitive)
     */
    List<InventoryItem> findByNameContainingIgnoreCase(String name);

    /**
     * Find items with low stock
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock <= i.minimumStockLevel AND i.status = 'ACTIVE'")
    List<InventoryItem> findLowStockItems();

    /**
     * Find items expiring soon
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.status = 'ACTIVE'")
    List<InventoryItem> findItemsExpiringSoon(@Param("date") LocalDateTime date);

    /**
     * Find expired items
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate <= :date")
    List<InventoryItem> findExpiredItems(@Param("date") LocalDateTime date);

    /**
     * Find items by storage location
     */
    List<InventoryItem> findByStorageLocationContainingIgnoreCase(String storageLocation);

    /**
     * Find items by lot number
     */
    List<InventoryItem> findByLotNumber(String lotNumber);

    /**
     * Find items by category with pagination
     */
    Page<InventoryItem> findByCategory(InventoryCategory category, Pageable pageable);

    /**
     * Find items by status with pagination
     */
    Page<InventoryItem> findByStatus(InventoryStatus status, Pageable pageable);

    /**
     * Search items by multiple criteria
     */
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:sku IS NULL OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:supplier IS NULL OR LOWER(i.supplier) LIKE LOWER(CONCAT('%', :supplier, '%')))")
    Page<InventoryItem> searchItems(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("category") InventoryCategory category,
            @Param("status") InventoryStatus status,
            @Param("supplier") String supplier,
            Pageable pageable);

    /**
     * Count items by category
     */
    long countByCategory(InventoryCategory category);

    /**
     * Count items by status
     */
    long countByStatus(InventoryStatus status);

    /**
     * Count low stock items
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.currentStock <= i.minimumStockLevel AND i.status = 'ACTIVE'")
    long countLowStockItems();

    /**
     * Count items expiring soon
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.expiryDate IS NOT NULL AND i.expiryDate <= :date AND i.status = 'ACTIVE'")
    long countItemsExpiringSoon(@Param("date") LocalDateTime date);

    /**
     * Get inventory statistics
     */
    @Query("SELECT i.category, COUNT(i), SUM(i.currentStock) FROM InventoryItem i WHERE i.status = 'ACTIVE' GROUP BY i.category")
    List<Object[]> getInventoryStatisticsByCategory();

    @Query("SELECT i.status, COUNT(i) FROM InventoryItem i GROUP BY i.status")
    List<Object[]> getInventoryStatusStatistics();

    /**
     * Calculate total inventory value
     */
    @Query("SELECT SUM(i.currentStock * i.unitCost) FROM InventoryItem i WHERE i.status = 'ACTIVE' AND i.unitCost IS NOT NULL")
    Double getTotalInventoryValue();

    /**
     * Calculate inventory value by category
     */
    @Query("SELECT i.category, SUM(i.currentStock * i.unitCost) FROM InventoryItem i WHERE i.status = 'ACTIVE' AND i.unitCost IS NOT NULL GROUP BY i.category")
    List<Object[]> getInventoryValueByCategory();

    /**
     * Find items requiring reorder
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.reorderPoint IS NOT NULL AND i.currentStock <= i.reorderPoint AND i.status = 'ACTIVE'")
    List<InventoryItem> findItemsRequiringReorder();
}
