package com.sivalab.laboperations.entity;

/**
 * NABL-compliant sample types for medical laboratory testing
 */
public enum SampleType {
    // Blood samples
    WHOLE_BLOOD("Whole Blood", "WB", "EDTA tube", 2.0, 5.0),
    SERUM("Serum", "SER", "Serum separator tube", 1.0, 3.0),
    PLASMA("Plasma", "PLA", "EDTA/Heparin tube", 1.0, 3.0),
    
    // Urine samples
    RANDOM_URINE("Random Urine", "RU", "Sterile container", 10.0, 50.0),
    FIRST_MORNING_URINE("First Morning Urine", "FMU", "Sterile container", 10.0, 50.0),
    MIDSTREAM_URINE("Midstream Urine", "MSU", "Sterile container", 10.0, 50.0),
    TWENTY_FOUR_HOUR_URINE("24-Hour Urine", "24HU", "Large container", 500.0, 3000.0),
    
    // Other body fluids
    CEREBROSPINAL_FLUID("Cerebrospinal Fluid", "CSF", "Sterile tube", 0.5, 2.0),
    SYNOVIAL_FLUID("Synovial Fluid", "SF", "Sterile tube", 0.5, 2.0),
    PLEURAL_FLUID("Pleural Fluid", "PF", "Sterile tube", 1.0, 5.0),
    ASCITIC_FLUID("Ascitic Fluid", "AF", "Sterile tube", 1.0, 5.0),
    
    // Swabs and cultures
    THROAT_SWAB("Throat Swab", "TS", "Transport medium", 0.0, 0.0),
    NASAL_SWAB("Nasal Swab", "NS", "Transport medium", 0.0, 0.0),
    WOUND_SWAB("Wound Swab", "WS", "Transport medium", 0.0, 0.0),
    VAGINAL_SWAB("Vaginal Swab", "VS", "Transport medium", 0.0, 0.0),
    
    // Stool samples
    STOOL("Stool", "ST", "Stool container", 2.0, 10.0),
    
    // Sputum
    SPUTUM("Sputum", "SP", "Sterile container", 2.0, 10.0),
    
    // Tissue samples
    TISSUE_BIOPSY("Tissue Biopsy", "TB", "Formalin container", 0.1, 5.0),
    
    // Special samples
    SALIVA("Saliva", "SAL", "Sterile tube", 1.0, 5.0),
    HAIR("Hair", "HR", "Envelope", 0.0, 0.0),
    NAIL("Nail", "NL", "Envelope", 0.0, 0.0);
    
    private final String displayName;
    private final String code;
    private final String preferredContainer;
    private final Double minimumVolume; // in mL
    private final Double optimalVolume; // in mL
    
    SampleType(String displayName, String code, String preferredContainer, 
               Double minimumVolume, Double optimalVolume) {
        this.displayName = displayName;
        this.code = code;
        this.preferredContainer = preferredContainer;
        this.minimumVolume = minimumVolume;
        this.optimalVolume = optimalVolume;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getPreferredContainer() {
        return preferredContainer;
    }
    
    public Double getMinimumVolume() {
        return minimumVolume;
    }
    
    public Double getOptimalVolume() {
        return optimalVolume;
    }
    
    /**
     * Get sample type by code
     */
    public static SampleType fromCode(String code) {
        for (SampleType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sample type code: " + code);
    }
    
    /**
     * Check if sample requires special handling
     */
    public boolean requiresSpecialHandling() {
        return this == CEREBROSPINAL_FLUID || 
               this == SYNOVIAL_FLUID || 
               this == PLEURAL_FLUID || 
               this == ASCITIC_FLUID ||
               this == TISSUE_BIOPSY;
    }
    
    /**
     * Check if sample requires refrigeration
     */
    public boolean requiresRefrigeration() {
        return this == WHOLE_BLOOD || 
               this == SERUM || 
               this == PLASMA ||
               this == TWENTY_FOUR_HOUR_URINE ||
               this == CEREBROSPINAL_FLUID ||
               this == SPUTUM;
    }
    
    /**
     * Get storage temperature range
     */
    public String getStorageTemperature() {
        if (requiresRefrigeration()) {
            return "2-8°C";
        } else if (this == TISSUE_BIOPSY) {
            return "Room temperature (formalin)";
        } else {
            return "Room temperature";
        }
    }
    
    /**
     * Get maximum storage duration
     */
    public String getMaxStorageDuration() {
        switch (this) {
            case WHOLE_BLOOD:
                return "24 hours";
            case SERUM:
            case PLASMA:
                return "7 days (refrigerated)";
            case RANDOM_URINE:
            case FIRST_MORNING_URINE:
            case MIDSTREAM_URINE:
                return "4 hours (room temp), 24 hours (refrigerated)";
            case TWENTY_FOUR_HOUR_URINE:
                return "24 hours (with preservative)";
            case CEREBROSPINAL_FLUID:
                return "Immediate processing required";
            case STOOL:
                return "2 hours (room temp), 24 hours (refrigerated)";
            case SPUTUM:
                return "2 hours (room temp), 24 hours (refrigerated)";
            case TISSUE_BIOPSY:
                return "Indefinite (in formalin)";
            default:
                return "Follow standard protocols";
        }
    }
}
