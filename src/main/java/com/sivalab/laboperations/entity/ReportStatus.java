package com.sivalab.laboperations.entity;

/**
 * NABL 112 compliant report status workflow
 */
public enum ReportStatus {
    DRAFT("Draft - Report being prepared"),
    GENERATED("Generated - Report created but not authorized"),
    AUTHORIZED("Authorized - Report approved by authorized signatory"),
    SENT("Sent - Report delivered to patient/physician");
    
    private final String description;
    
    ReportStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getValue() {
        return this.name();
    }
}
