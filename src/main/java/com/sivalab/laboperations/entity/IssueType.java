package com.sivalab.laboperations.entity;

/**
 * Enum representing types of machine ID issues
 */
public enum IssueType {
    MACHINE_ID_MISMATCH("Machine ID Mismatch"),
    MACHINE_ID_DUPLICATE("Duplicate Machine ID"),
    MACHINE_ID_MISSING("Missing Machine ID"),
    MACHINE_ID_INVALID_FORMAT("Invalid Machine ID Format"),
    MAC_ADDRESS_CONFLICT("MAC Address Conflict"),
    MAC_ADDRESS_SPOOFING("MAC Address Spoofing"),
    IP_ADDRESS_CONFLICT("IP Address Conflict"),
    DHCP_LEASE_EXPIRED("DHCP Lease Expired"),
    DNS_RESOLUTION_FAILED("DNS Resolution Failed"),
    NETWORK_AUTHENTICATION_FAILED("Network Authentication Failed"),
    CERTIFICATE_EXPIRED("Security Certificate Expired"),
    CERTIFICATE_INVALID("Invalid Security Certificate"),
    FIRMWARE_OUTDATED("Outdated Firmware"),
    DRIVER_INCOMPATIBLE("Incompatible Network Driver"),
    HARDWARE_FAILURE("Network Hardware Failure"),
    SIGNAL_INTERFERENCE("Signal Interference"),
    BANDWIDTH_LIMITATION("Bandwidth Limitation"),
    CONNECTION_TIMEOUT("Connection Timeout"),
    PACKET_LOSS_HIGH("High Packet Loss"),
    LATENCY_HIGH("High Network Latency"),
    SECURITY_BREACH_DETECTED("Security Breach Detected"),
    UNAUTHORIZED_ACCESS_ATTEMPT("Unauthorized Access Attempt"),
    CONFIGURATION_ERROR("Network Configuration Error"),
    POWER_MANAGEMENT_ISSUE("Power Management Issue"),
    ADAPTER_NOT_RECOGNIZED("Network Adapter Not Recognized"),
    PROFILE_CORRUPTION("Network Profile Corruption"),
    REGISTRY_CORRUPTION("Network Registry Corruption"),
    SERVICE_UNAVAILABLE("Network Service Unavailable"),
    PROTOCOL_ERROR("Network Protocol Error"),
    FIREWALL_BLOCKING("Firewall Blocking Connection"),
    PROXY_CONFIGURATION_ERROR("Proxy Configuration Error"),
    VPN_CONNECTION_FAILED("VPN Connection Failed"),
    MESH_NETWORK_ISSUE("Mesh Network Issue"),
    ROAMING_FAILURE("Network Roaming Failure"),
    QOS_VIOLATION("Quality of Service Violation"),
    COMPLIANCE_VIOLATION("Network Compliance Violation"),
    MONITORING_FAILURE("Network Monitoring Failure"),
    BACKUP_CONNECTION_FAILED("Backup Connection Failed"),
    LOAD_BALANCING_ISSUE("Load Balancing Issue"),
    REDUNDANCY_FAILURE("Network Redundancy Failure"),
    OTHER("Other Issue");

    private final String displayName;

    IssueType(String displayName) {
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
