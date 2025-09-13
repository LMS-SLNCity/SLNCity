package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing inventory items (reagents, consumables, etc.)
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "SKU is required")
    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InventoryCategory category;

    @NotBlank(message = "Unit of measurement is required")
    @Column(name = "unit_of_measurement", nullable = false)
    private String unitOfMeasurement;

    @NotNull(message = "Current stock is required")
    @PositiveOrZero(message = "Current stock must be zero or positive")
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @NotNull(message = "Minimum stock level is required")
    @PositiveOrZero(message = "Minimum stock level must be zero or positive")
    @Column(name = "minimum_stock_level", nullable = false)
    private Integer minimumStockLevel = 0;

    @NotNull(message = "Maximum stock level is required")
    @PositiveOrZero(message = "Maximum stock level must be zero or positive")
    @Column(name = "maximum_stock_level", nullable = false)
    private Integer maximumStockLevel = 100;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "supplier_catalog_number")
    private String supplierCatalogNumber;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(name = "storage_conditions")
    private String storageConditions;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "lot_number")
    private String lotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InventoryStatus status = InventoryStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> transactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public InventoryItem() {}

    public InventoryItem(String name, String sku, InventoryCategory category, String unitOfMeasurement) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public InventoryCategory getCategory() { return category; }
    public void setCategory(InventoryCategory category) { this.category = category; }

    public String getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(String unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(Integer minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public Integer getMaximumStockLevel() { return maximumStockLevel; }
    public void setMaximumStockLevel(Integer maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getSupplierCatalogNumber() { return supplierCatalogNumber; }
    public void setSupplierCatalogNumber(String supplierCatalogNumber) { this.supplierCatalogNumber = supplierCatalogNumber; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getStorageConditions() { return storageConditions; }
    public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public InventoryStatus getStatus() { return status; }
    public void setStatus(InventoryStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<InventoryTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<InventoryTransaction> transactions) { this.transactions = transactions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isLowStock() {
        return currentStock <= minimumStockLevel;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now().plusDays(daysThreshold));
    }

    public BigDecimal getTotalValue() {
        if (unitCost != null && currentStock != null) {
            return unitCost.multiply(BigDecimal.valueOf(currentStock));
        }
        return BigDecimal.ZERO;
    }
}
