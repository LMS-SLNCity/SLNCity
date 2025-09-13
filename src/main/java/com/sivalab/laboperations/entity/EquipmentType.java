package com.sivalab.laboperations.entity;

/**
 * Enumeration for different types of laboratory equipment
 */
public enum EquipmentType {
    // Analytical Equipment
    SPECTROPHOTOMETER("Spectrophotometer", "Analytical"),
    MICROSCOPE("Microscope", "Analytical"),
    CENTRIFUGE("Centrifuge", "Analytical"),
    ANALYZER("Analyzer", "Analytical"),
    CHROMATOGRAPH("Chromatograph", "Analytical"),
    ELECTROPHORESIS("Electrophoresis", "Analytical"),
    
    // Sample Processing
    PIPETTE("Pipette", "Sample Processing"),
    DISPENSER("Dispenser", "Sample Processing"),
    MIXER("Mixer", "Sample Processing"),
    HOMOGENIZER("Homogenizer", "Sample Processing"),
    SONICATOR("Sonicator", "Sample Processing"),
    
    // Incubation & Storage
    INCUBATOR("Incubator", "Incubation & Storage"),
    REFRIGERATOR("Refrigerator", "Incubation & Storage"),
    FREEZER("Freezer", "Incubation & Storage"),
    WATER_BATH("Water Bath", "Incubation & Storage"),
    DRY_BATH("Dry Bath", "Incubation & Storage"),
    
    // Safety Equipment
    BIOSAFETY_CABINET("Biosafety Cabinet", "Safety"),
    FUME_HOOD("Fume Hood", "Safety"),
    AUTOCLAVE("Autoclave", "Safety"),
    STERILIZER("Sterilizer", "Safety"),
    
    // Measurement & Weighing
    BALANCE("Balance", "Measurement"),
    SCALE("Scale", "Measurement"),
    PH_METER("pH Meter", "Measurement"),
    THERMOMETER("Thermometer", "Measurement"),
    
    // General Equipment
    PRINTER("Printer", "General"),
    COMPUTER("Computer", "General"),
    BARCODE_SCANNER("Barcode Scanner", "General"),
    LABEL_PRINTER("Label Printer", "General"),
    
    // Other
    OTHER("Other", "Other");

    private final String displayName;
    private final String category;

    EquipmentType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
