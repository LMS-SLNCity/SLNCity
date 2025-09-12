package com.sivalab.laboperations.entity;

/**
 * NABL-compliant sample status lifecycle
 * Follows NABL 112 requirements for complete sample tracking
 */
public enum SampleStatus {
    // Collection phase
    COLLECTED("Collected", "Sample collected from patient", 1),
    
    // Transport phase
    IN_TRANSIT("In Transit", "Sample being transported to laboratory", 2),
    
    // Receipt phase
    RECEIVED("Received", "Sample received at laboratory", 3),
    ACCESSIONED("Accessioned", "Sample logged into laboratory system", 4),
    
    // Quality control phase
    ACCEPTED("Accepted", "Sample passed quality control checks", 5),
    REJECTED("Rejected", "Sample failed quality control - unsuitable for testing", 6),
    
    // Processing phase
    PROCESSING("Processing", "Sample being prepared for analysis", 7),
    ALIQUOTED("Aliquoted", "Sample divided into portions for different tests", 8),
    
    // Analysis phase
    IN_ANALYSIS("In Analysis", "Sample currently being analyzed", 9),
    ANALYSIS_COMPLETE("Analysis Complete", "All requested tests completed", 10),
    
    // Review phase
    UNDER_REVIEW("Under Review", "Results being reviewed by qualified personnel", 11),
    REVIEWED("Reviewed", "Results reviewed and approved", 12),
    
    // Storage phase
    STORED("Stored", "Sample stored for retention period", 13),
    
    // Disposal phase
    DISPOSED("Disposed", "Sample disposed according to protocols", 14),
    
    // Special statuses
    ON_HOLD("On Hold", "Sample processing temporarily suspended", 15),
    RECALLED("Recalled", "Sample recalled for additional testing", 16);
    
    private final String displayName;
    private final String description;
    private final int sequenceOrder;
    
    SampleStatus(String displayName, String description, int sequenceOrder) {
        this.displayName = displayName;
        this.description = description;
        this.sequenceOrder = sequenceOrder;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getSequenceOrder() {
        return sequenceOrder;
    }
    
    /**
     * Check if status transition is valid according to NABL requirements
     */
    public boolean canTransitionTo(SampleStatus newStatus) {
        // Special cases
        if (this == REJECTED) {
            return false; // Rejected samples cannot transition to other states
        }
        
        if (this == DISPOSED) {
            return false; // Disposed samples cannot transition to other states
        }
        
        if (newStatus == ON_HOLD) {
            return this != REJECTED && this != DISPOSED; // Can go on hold from any valid state
        }
        
        if (this == ON_HOLD) {
            return true; // Can transition from hold to any state
        }
        
        if (newStatus == RECALLED) {
            return this == STORED || this == DISPOSED; // Can only recall stored or disposed samples
        }
        
        if (newStatus == REJECTED) {
            return this.sequenceOrder <= ACCEPTED.sequenceOrder; // Can only reject before processing
        }
        
        // Normal progression - can only move forward or stay same
        return newStatus.sequenceOrder >= this.sequenceOrder;
    }
    
    /**
     * Get next possible statuses
     */
    public SampleStatus[] getNextPossibleStatuses() {
        switch (this) {
            case COLLECTED:
                return new SampleStatus[]{IN_TRANSIT, RECEIVED, REJECTED};
            case IN_TRANSIT:
                return new SampleStatus[]{RECEIVED, REJECTED};
            case RECEIVED:
                return new SampleStatus[]{ACCESSIONED, REJECTED};
            case ACCESSIONED:
                return new SampleStatus[]{ACCEPTED, REJECTED};
            case ACCEPTED:
                return new SampleStatus[]{PROCESSING};
            case PROCESSING:
                return new SampleStatus[]{ALIQUOTED, IN_ANALYSIS};
            case ALIQUOTED:
                return new SampleStatus[]{IN_ANALYSIS};
            case IN_ANALYSIS:
                return new SampleStatus[]{ANALYSIS_COMPLETE};
            case ANALYSIS_COMPLETE:
                return new SampleStatus[]{UNDER_REVIEW};
            case UNDER_REVIEW:
                return new SampleStatus[]{REVIEWED};
            case REVIEWED:
                return new SampleStatus[]{STORED};
            case STORED:
                return new SampleStatus[]{DISPOSED, RECALLED};
            case RECALLED:
                return new SampleStatus[]{PROCESSING, IN_ANALYSIS};
            case ON_HOLD:
                return new SampleStatus[]{PROCESSING, IN_ANALYSIS, UNDER_REVIEW, STORED};
            case REJECTED:
            case DISPOSED:
                return new SampleStatus[]{}; // Terminal states
            default:
                return new SampleStatus[]{};
        }
    }
    
    /**
     * Check if status is terminal (no further transitions possible)
     */
    public boolean isTerminal() {
        return this == REJECTED || this == DISPOSED;
    }
    
    /**
     * Check if status indicates sample is available for testing
     */
    public boolean isAvailableForTesting() {
        return this == ACCEPTED || 
               this == PROCESSING || 
               this == ALIQUOTED || 
               this == IN_ANALYSIS;
    }
    
    /**
     * Check if status indicates testing is complete
     */
    public boolean isTestingComplete() {
        return this == ANALYSIS_COMPLETE || 
               this == UNDER_REVIEW || 
               this == REVIEWED || 
               this == STORED || 
               this == DISPOSED;
    }
    
    /**
     * Get status color for UI display
     */
    public String getStatusColor() {
        switch (this) {
            case COLLECTED:
            case IN_TRANSIT:
            case RECEIVED:
            case ACCESSIONED:
                return "#FFA500"; // Orange - In progress
            case ACCEPTED:
            case PROCESSING:
            case ALIQUOTED:
            case IN_ANALYSIS:
                return "#0066CC"; // Blue - Active processing
            case ANALYSIS_COMPLETE:
            case UNDER_REVIEW:
                return "#FF6600"; // Dark orange - Pending review
            case REVIEWED:
            case STORED:
                return "#00AA00"; // Green - Complete
            case DISPOSED:
                return "#666666"; // Gray - Disposed
            case REJECTED:
                return "#CC0000"; // Red - Rejected
            case ON_HOLD:
                return "#FFCC00"; // Yellow - On hold
            case RECALLED:
                return "#9900CC"; // Purple - Recalled
            default:
                return "#000000"; // Black - Unknown
        }
    }
    
    /**
     * Get NABL compliance requirements for this status
     */
    public String[] getNablRequirements() {
        switch (this) {
            case COLLECTED:
                return new String[]{
                    "Record collection date and time",
                    "Record collector identification",
                    "Document collection conditions",
                    "Verify patient identification"
                };
            case RECEIVED:
                return new String[]{
                    "Record receipt date and time",
                    "Record receiver identification",
                    "Check sample integrity",
                    "Verify sample identification"
                };
            case ACCEPTED:
                return new String[]{
                    "Verify sample quality",
                    "Check volume adequacy",
                    "Confirm container type",
                    "Document acceptance criteria"
                };
            case REJECTED:
                return new String[]{
                    "Document rejection reason",
                    "Record rejecting personnel",
                    "Notify requesting physician",
                    "Follow rejection protocol"
                };
            case REVIEWED:
                return new String[]{
                    "Technical review completed",
                    "Results validated",
                    "Quality control verified",
                    "Authorized by qualified personnel"
                };
            case DISPOSED:
                return new String[]{
                    "Follow disposal protocol",
                    "Record disposal method",
                    "Document disposal date",
                    "Maintain disposal records"
                };
            default:
                return new String[]{
                    "Follow standard operating procedures",
                    "Maintain complete documentation",
                    "Ensure traceability"
                };
        }
    }
}
