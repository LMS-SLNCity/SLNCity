package com.sivalab.laboperations.entity;

/**
 * NABL 112 compliant report types
 */
public enum ReportType {
    STANDARD("Standard Report"),
    AMENDED("Amended Report"),
    SUPPLEMENTARY("Supplementary Report");
    
    private final String description;
    
    ReportType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getValue() {
        return this.name();
    }
}
