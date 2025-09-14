package com.sivalab.laboperations.entity;

/**
 * Enum representing status of machine ID issues
 */
public enum IssueStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    PENDING_APPROVAL("Pending Approval"),
    WAITING_FOR_PARTS("Waiting for Parts"),
    WAITING_FOR_VENDOR("Waiting for Vendor"),
    ESCALATED("Escalated"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    CANCELLED("Cancelled"),
    DUPLICATE("Duplicate"),
    DEFERRED("Deferred");

    private final String displayName;

    IssueStatus(String displayName) {
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
