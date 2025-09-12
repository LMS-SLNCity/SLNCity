package com.sivalab.laboperations.entity;

public enum TestStatus {
    PENDING("pending"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed"),
    APPROVED("approved");
    
    private final String value;
    
    TestStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TestStatus fromValue(String value) {
        for (TestStatus status : TestStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown test status: " + value);
    }
}
