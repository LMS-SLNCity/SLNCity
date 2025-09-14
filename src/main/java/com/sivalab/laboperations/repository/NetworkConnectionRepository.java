package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.NetworkConnection;
import com.sivalab.laboperations.entity.ConnectionStatus;
import com.sivalab.laboperations.entity.ConnectionType;
import com.sivalab.laboperations.entity.LabEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NetworkConnection entity
 */
@Repository
public interface NetworkConnectionRepository extends JpaRepository<NetworkConnection, Long> {

    /**
     * Find network connection by machine ID
     */
    Optional<NetworkConnection> findByMachineId(String machineId);

    /**
     * Find network connection by MAC address
     */
    Optional<NetworkConnection> findByMacAddress(String macAddress);

    /**
     * Find network connection by IP address
     */
    Optional<NetworkConnection> findByIpAddress(String ipAddress);

    /**
     * Find network connections by equipment
     */
    List<NetworkConnection> findByEquipment(LabEquipment equipment);

    /**
     * Find network connections by equipment ID
     */
    List<NetworkConnection> findByEquipmentId(Long equipmentId);

    /**
     * Find network connections by connection status
     */
    List<NetworkConnection> findByConnectionStatus(ConnectionStatus status);

    /**
     * Find network connections by connection type
     */
    List<NetworkConnection> findByConnectionType(ConnectionType type);

    /**
     * Find network connections by SSID
     */
    List<NetworkConnection> findBySsid(String ssid);

    /**
     * Find connected network connections
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.connectionStatus = 'CONNECTED'")
    List<NetworkConnection> findConnectedConnections();

    /**
     * Find disconnected network connections
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.connectionStatus = 'DISCONNECTED'")
    List<NetworkConnection> findDisconnectedConnections();

    /**
     * Find network connections with weak signal
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.signalStrength < :threshold")
    List<NetworkConnection> findConnectionsWithWeakSignal(@Param("threshold") Integer threshold);

    /**
     * Find network connections with high latency
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.lastPingResponseMs > :threshold")
    List<NetworkConnection> findConnectionsWithHighLatency(@Param("threshold") Integer threshold);

    /**
     * Find network connections with high packet loss
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.packetLossPercentage > :threshold")
    List<NetworkConnection> findConnectionsWithHighPacketLoss(@Param("threshold") Double threshold);

    /**
     * Find network connections requiring attention
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE " +
           "nc.connectionStatus IN ('FAILED', 'TIMEOUT', 'AUTHENTICATION_FAILED', 'WEAK_SIGNAL', 'LIMITED_CONNECTIVITY') " +
           "OR nc.signalStrength < -70 " +
           "OR nc.lastPingResponseMs > 500 " +
           "OR nc.packetLossPercentage > 5.0")
    List<NetworkConnection> findConnectionsRequiringAttention();

    /**
     * Find high priority network connections
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.priorityLevel = 1")
    List<NetworkConnection> findHighPriorityConnections();

    /**
     * Find network connections with auto-reconnect enabled
     */
    List<NetworkConnection> findByAutoReconnectEnabled(Boolean enabled);

    /**
     * Find network connections by DHCP enabled status
     */
    List<NetworkConnection> findByDhcpEnabled(Boolean enabled);

    /**
     * Find network connections last connected before a specific date
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.lastConnected < :dateTime")
    List<NetworkConnection> findConnectionsLastConnectedBefore(@Param("dateTime") LocalDateTime dateTime);

    /**
     * Find network connections with connection errors above threshold
     */
    @Query("SELECT nc FROM NetworkConnection nc WHERE nc.connectionErrorsCount > :threshold")
    List<NetworkConnection> findConnectionsWithHighErrorCount(@Param("threshold") Integer threshold);

    /**
     * Get network connection statistics by status
     */
    @Query("SELECT nc.connectionStatus, COUNT(nc) FROM NetworkConnection nc GROUP BY nc.connectionStatus")
    List<Object[]> getConnectionStatusStatistics();

    /**
     * Get network connection statistics by type
     */
    @Query("SELECT nc.connectionType, COUNT(nc) FROM NetworkConnection nc GROUP BY nc.connectionType")
    List<Object[]> getConnectionTypeStatistics();

    /**
     * Get network connection statistics by SSID
     */
    @Query("SELECT nc.ssid, COUNT(nc) FROM NetworkConnection nc WHERE nc.ssid IS NOT NULL GROUP BY nc.ssid")
    List<Object[]> getConnectionSsidStatistics();

    /**
     * Get average signal strength by connection type
     */
    @Query("SELECT nc.connectionType, AVG(nc.signalStrength) FROM NetworkConnection nc " +
           "WHERE nc.signalStrength IS NOT NULL GROUP BY nc.connectionType")
    List<Object[]> getAverageSignalStrengthByType();

    /**
     * Get average bandwidth by connection type
     */
    @Query("SELECT nc.connectionType, AVG(nc.bandwidthMbps) FROM NetworkConnection nc " +
           "WHERE nc.bandwidthMbps IS NOT NULL GROUP BY nc.connectionType")
    List<Object[]> getAverageBandwidthByType();

    /**
     * Count connections by equipment status
     */
    @Query("SELECT e.status, COUNT(nc) FROM NetworkConnection nc " +
           "JOIN nc.equipment e GROUP BY e.status")
    List<Object[]> getConnectionCountByEquipmentStatus();

    /**
     * Find duplicate machine IDs
     */
    @Query("SELECT nc.machineId, COUNT(nc) FROM NetworkConnection nc " +
           "GROUP BY nc.machineId HAVING COUNT(nc) > 1")
    List<Object[]> findDuplicateMachineIds();

    /**
     * Find duplicate MAC addresses
     */
    @Query("SELECT nc.macAddress, COUNT(nc) FROM NetworkConnection nc " +
           "GROUP BY nc.macAddress HAVING COUNT(nc) > 1")
    List<Object[]> findDuplicateMacAddresses();

    /**
     * Find duplicate IP addresses
     */
    @Query("SELECT nc.ipAddress, COUNT(nc) FROM NetworkConnection nc " +
           "WHERE nc.ipAddress IS NOT NULL " +
           "GROUP BY nc.ipAddress HAVING COUNT(nc) > 1")
    List<Object[]> findDuplicateIpAddresses();

    /**
     * Check if machine ID exists
     */
    boolean existsByMachineId(String machineId);

    /**
     * Check if MAC address exists
     */
    boolean existsByMacAddress(String macAddress);

    /**
     * Check if IP address exists
     */
    boolean existsByIpAddress(String ipAddress);
}
