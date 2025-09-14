package com.sivalab.laboperations.entity;

/**
 * Enum representing network connection types
 */
public enum ConnectionType {
    WLAN("Wireless LAN"),
    ETHERNET("Ethernet"),
    BLUETOOTH("Bluetooth"),
    USB("USB Connection"),
    SERIAL("Serial Connection"),
    CELLULAR("Cellular"),
    SATELLITE("Satellite"),
    VPN("VPN Connection"),
    HOTSPOT("Mobile Hotspot"),
    MESH("Mesh Network");

    private final String displayName;

    ConnectionType(String displayName) {
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
