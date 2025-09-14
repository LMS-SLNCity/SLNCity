package com.sivalab.laboperations.entity;

/**
 * Enum representing network connection status
 */
public enum ConnectionStatus {
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    RECONNECTING("Reconnecting"),
    FAILED("Connection Failed"),
    TIMEOUT("Connection Timeout"),
    AUTHENTICATION_FAILED("Authentication Failed"),
    WEAK_SIGNAL("Weak Signal"),
    LIMITED_CONNECTIVITY("Limited Connectivity"),
    NO_INTERNET("No Internet Access"),
    MAINTENANCE("Under Maintenance");

    private final String displayName;

    ConnectionStatus(String displayName) {
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
