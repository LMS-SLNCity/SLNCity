package com.sivalab.laboperations.entity;

/**
 * Enumeration for inventory item status
 */
public enum InventoryStatus {
    ACTIVE("Active", "Item is available for use"),
    INACTIVE("Inactive", "Item is not currently available"),
    EXPIRED("Expired", "Item has passed its expiry date"),
    RECALLED("Recalled", "Item has been recalled by manufacturer"),
    QUARANTINED("Quarantined", "Item is under quality review"),
    DISCONTINUED("Discontinued", "Item is no longer available from supplier"),
    DAMAGED("Damaged", "Item is damaged and unusable");

    private final String displayName;
    private final String description;

    InventoryStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
