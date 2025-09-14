package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing network connections for lab equipment
 */
@Entity
@Table(name = "network_connections")
public class NetworkConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Equipment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private LabEquipment equipment;

    @NotBlank(message = "Machine ID is required")
    @Column(name = "machine_id", nullable = false, unique = true)
    private String machineId;

    @NotBlank(message = "MAC address is required")
    @Column(name = "mac_address", nullable = false)
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "ssid")
    private String ssid;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private ConnectionType connectionType = ConnectionType.WLAN;

    @Column(name = "signal_strength")
    private Integer signalStrength; // dBm

    @Column(name = "bandwidth_mbps")
    private Double bandwidthMbps;

    @Column(name = "last_connected")
    private LocalDateTime lastConnected;

    @Column(name = "last_disconnected")
    private LocalDateTime lastDisconnected;

    @Column(name = "connection_uptime_hours")
    private Double connectionUptimeHours;

    @Column(name = "total_data_transferred_mb")
    private Double totalDataTransferredMb;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "driver_version")
    private String driverVersion;

    @Column(name = "network_adapter_model")
    private String networkAdapterModel;

    @Column(name = "dns_servers")
    private String dnsServers;

    @Column(name = "gateway_address")
    private String gatewayAddress;

    @Column(name = "subnet_mask")
    private String subnetMask;

    @Column(name = "dhcp_enabled")
    private Boolean dhcpEnabled = true;

    @Column(name = "security_protocol")
    private String securityProtocol;

    @Column(name = "encryption_type")
    private String encryptionType;

    @Column(name = "last_ping_response_ms")
    private Integer lastPingResponseMs;

    @Column(name = "packet_loss_percentage")
    private Double packetLossPercentage;

    @Column(name = "connection_errors_count")
    private Integer connectionErrorsCount = 0;

    @Column(name = "auto_reconnect_enabled")
    private Boolean autoReconnectEnabled = true;

    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1=High, 2=Medium, 3=Low

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "network_diagnostics", columnDefinition = "json")
    private JsonNode networkDiagnostics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "connection_history", columnDefinition = "json")
    private JsonNode connectionHistory;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public NetworkConnection() {}

    public NetworkConnection(LabEquipment equipment, String machineId, String macAddress) {
        this.equipment = equipment;
        this.machineId = machineId;
        this.macAddress = macAddress;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LabEquipment getEquipment() { return equipment; }
    public void setEquipment(LabEquipment equipment) { this.equipment = equipment; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }

    public ConnectionStatus getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(ConnectionStatus connectionStatus) { this.connectionStatus = connectionStatus; }

    public ConnectionType getConnectionType() { return connectionType; }
    public void setConnectionType(ConnectionType connectionType) { this.connectionType = connectionType; }

    public Integer getSignalStrength() { return signalStrength; }
    public void setSignalStrength(Integer signalStrength) { this.signalStrength = signalStrength; }

    public Double getBandwidthMbps() { return bandwidthMbps; }
    public void setBandwidthMbps(Double bandwidthMbps) { this.bandwidthMbps = bandwidthMbps; }

    public LocalDateTime getLastConnected() { return lastConnected; }
    public void setLastConnected(LocalDateTime lastConnected) { this.lastConnected = lastConnected; }

    public LocalDateTime getLastDisconnected() { return lastDisconnected; }
    public void setLastDisconnected(LocalDateTime lastDisconnected) { this.lastDisconnected = lastDisconnected; }

    public Double getConnectionUptimeHours() { return connectionUptimeHours; }
    public void setConnectionUptimeHours(Double connectionUptimeHours) { this.connectionUptimeHours = connectionUptimeHours; }

    public Double getTotalDataTransferredMb() { return totalDataTransferredMb; }
    public void setTotalDataTransferredMb(Double totalDataTransferredMb) { this.totalDataTransferredMb = totalDataTransferredMb; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public String getDriverVersion() { return driverVersion; }
    public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }

    public String getNetworkAdapterModel() { return networkAdapterModel; }
    public void setNetworkAdapterModel(String networkAdapterModel) { this.networkAdapterModel = networkAdapterModel; }

    public String getDnsServers() { return dnsServers; }
    public void setDnsServers(String dnsServers) { this.dnsServers = dnsServers; }

    public String getGatewayAddress() { return gatewayAddress; }
    public void setGatewayAddress(String gatewayAddress) { this.gatewayAddress = gatewayAddress; }

    public String getSubnetMask() { return subnetMask; }
    public void setSubnetMask(String subnetMask) { this.subnetMask = subnetMask; }

    public Boolean getDhcpEnabled() { return dhcpEnabled; }
    public void setDhcpEnabled(Boolean dhcpEnabled) { this.dhcpEnabled = dhcpEnabled; }

    public String getSecurityProtocol() { return securityProtocol; }
    public void setSecurityProtocol(String securityProtocol) { this.securityProtocol = securityProtocol; }

    public String getEncryptionType() { return encryptionType; }
    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }

    public Integer getLastPingResponseMs() { return lastPingResponseMs; }
    public void setLastPingResponseMs(Integer lastPingResponseMs) { this.lastPingResponseMs = lastPingResponseMs; }

    public Double getPacketLossPercentage() { return packetLossPercentage; }
    public void setPacketLossPercentage(Double packetLossPercentage) { this.packetLossPercentage = packetLossPercentage; }

    public Integer getConnectionErrorsCount() { return connectionErrorsCount; }
    public void setConnectionErrorsCount(Integer connectionErrorsCount) { this.connectionErrorsCount = connectionErrorsCount; }

    public Boolean getAutoReconnectEnabled() { return autoReconnectEnabled; }
    public void setAutoReconnectEnabled(Boolean autoReconnectEnabled) { this.autoReconnectEnabled = autoReconnectEnabled; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public JsonNode getNetworkDiagnostics() { return networkDiagnostics; }
    public void setNetworkDiagnostics(JsonNode networkDiagnostics) { this.networkDiagnostics = networkDiagnostics; }

    public JsonNode getConnectionHistory() { return connectionHistory; }
    public void setConnectionHistory(JsonNode connectionHistory) { this.connectionHistory = connectionHistory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isConnected() {
        return connectionStatus == ConnectionStatus.CONNECTED;
    }

    public boolean hasStrongSignal() {
        return signalStrength != null && signalStrength > -50; // dBm
    }

    public boolean hasGoodLatency() {
        return lastPingResponseMs != null && lastPingResponseMs < 100; // ms
    }

    public boolean hasLowPacketLoss() {
        return packetLossPercentage != null && packetLossPercentage < 1.0; // %
    }

    public boolean isHighPriority() {
        return priorityLevel != null && priorityLevel == 1;
    }
}
