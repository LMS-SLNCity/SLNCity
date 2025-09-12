package com.sivalab.laboperations.entity;

public enum VisitStatus {
    PENDING("pending"),
    IN_PROGRESS("in-progress"),
    AWAITING_APPROVAL("awaiting-approval"),
    APPROVED("approved"),
    BILLED("billed"),
    COMPLETED("completed");
    
    private final String value;
    
    VisitStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static VisitStatus fromValue(String value) {
        for (VisitStatus status : VisitStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown visit status: " + value);
    }
}
