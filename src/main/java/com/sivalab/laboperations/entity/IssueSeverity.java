package com.sivalab.laboperations.entity;

/**
 * Enum representing severity levels of machine ID issues
 */
public enum IssueSeverity {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    INFORMATIONAL("Informational");

    private final String displayName;

    IssueSeverity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
