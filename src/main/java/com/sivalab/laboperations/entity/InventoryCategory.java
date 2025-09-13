package com.sivalab.laboperations.entity;

/**
 * Enumeration for inventory item categories
 */
public enum InventoryCategory {
    // Reagents and Chemicals
    REAGENTS("Reagents", "Chemical reagents and solutions"),
    BUFFERS("Buffers", "Buffer solutions and pH adjusters"),
    STAINS("Stains", "Staining solutions and dyes"),
    STANDARDS("Standards", "Reference standards and controls"),
    CALIBRATORS("Calibrators", "Calibration materials"),
    
    // Consumables
    TUBES("Tubes", "Test tubes and sample containers"),
    PIPETTE_TIPS("Pipette Tips", "Disposable pipette tips"),
    PLATES("Plates", "Microplates and culture plates"),
    SLIDES("Slides", "Microscope slides and coverslips"),
    FILTERS("Filters", "Filtration supplies"),
    SYRINGES("Syringes", "Syringes and needles"),
    
    // Sample Collection
    COLLECTION_TUBES("Collection Tubes", "Blood collection tubes"),
    SWABS("Swabs", "Sample collection swabs"),
    CONTAINERS("Containers", "Sample storage containers"),
    TRANSPORT_MEDIA("Transport Media", "Sample transport solutions"),
    
    // Safety and PPE
    GLOVES("Gloves", "Protective gloves"),
    MASKS("Masks", "Face masks and respirators"),
    GOWNS("Gowns", "Lab coats and protective gowns"),
    EYEWEAR("Eyewear", "Safety glasses and goggles"),
    
    // Cleaning and Maintenance
    CLEANING_SUPPLIES("Cleaning Supplies", "Cleaning agents and disinfectants"),
    MAINTENANCE_PARTS("Maintenance Parts", "Equipment spare parts"),
    
    // Office and Administrative
    LABELS("Labels", "Sample and equipment labels"),
    FORMS("Forms", "Lab forms and documentation"),
    STATIONERY("Stationery", "Office supplies"),
    
    // Quality Control
    QC_MATERIALS("QC Materials", "Quality control samples"),
    PROFICIENCY_TESTING("Proficiency Testing", "External QA materials"),
    
    // Other
    OTHER("Other", "Miscellaneous items");

    private final String displayName;
    private final String description;

    InventoryCategory(String displayName, String description) {
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
