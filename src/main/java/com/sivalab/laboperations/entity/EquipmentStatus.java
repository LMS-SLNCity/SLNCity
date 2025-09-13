package com.sivalab.laboperations.entity;

/**
 * Enumeration for equipment status
 */
public enum EquipmentStatus {
    ACTIVE("Active", "Equipment is operational and available for use"),
    INACTIVE("Inactive", "Equipment is not currently in use"),
    MAINTENANCE("Under Maintenance", "Equipment is undergoing maintenance"),
    CALIBRATION("Under Calibration", "Equipment is being calibrated"),
    OUT_OF_ORDER("Out of Order", "Equipment is broken or malfunctioning"),
    RETIRED("Retired", "Equipment has been permanently removed from service"),
    RESERVED("Reserved", "Equipment is reserved for specific use");

    private final String displayName;
    private final String description;

    EquipmentStatus(String displayName, String description) {
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
