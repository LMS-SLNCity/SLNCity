package com.sivalab.laboperations.service;

import com.sivalab.laboperations.config.NetworkConfig;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.NetworkConnectionRepository;
import com.sivalab.laboperations.repository.LabEquipmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing network connections with fault tolerance
 * This service is optional and only enabled when network monitoring is configured
 */
@Service
@Transactional
@ConditionalOnProperty(name = "lab.network.enabled", havingValue = "true", matchIfMissing = false)
public class NetworkConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkConnectionService.class);

    @Autowired
    private NetworkConnectionRepository networkConnectionRepository;

    @Autowired
    private LabEquipmentRepository equipmentRepository;

    @Autowired
    private NetworkConfig networkConfig;

    /**
     * Create new network connection
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCreateConnection")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public NetworkConnection createConnection(NetworkConnection connection) {
        logger.info("Creating new network connection for machine ID: {}", connection.getMachineId());
        
        // Validate unique constraints
        if (networkConnectionRepository.existsByMachineId(connection.getMachineId())) {
            throw new IllegalArgumentException("Machine ID already exists: " + connection.getMachineId());
        }
        
        if (networkConnectionRepository.existsByMacAddress(connection.getMacAddress())) {
            throw new IllegalArgumentException("MAC address already exists: " + connection.getMacAddress());
        }
        
        return networkConnectionRepository.save(connection);
    }

    /**
     * Get connection by ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnection")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<NetworkConnection> getConnectionById(Long id) {
        logger.debug("Fetching network connection with ID: {}", id);
        return networkConnectionRepository.findById(id);
    }

    /**
     * Get connection by machine ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectionByMachineId")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<NetworkConnection> getConnectionByMachineId(String machineId) {
        logger.debug("Fetching network connection with machine ID: {}", machineId);
        return networkConnectionRepository.findByMachineId(machineId);
    }

    /**
     * Get connection by MAC address
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectionByMacAddress")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<NetworkConnection> getConnectionByMacAddress(String macAddress) {
        logger.debug("Fetching network connection with MAC address: {}", macAddress);
        return networkConnectionRepository.findByMacAddress(macAddress);
    }

    /**
     * Get all connections
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetAllConnections")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<NetworkConnection> getAllConnections() {
        logger.debug("Fetching all network connections");
        return networkConnectionRepository.findAll();
    }

    /**
     * Get connections by equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectionsByEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<NetworkConnection> getConnectionsByEquipment(Long equipmentId) {
        logger.debug("Fetching network connections for equipment ID: {}", equipmentId);
        return networkConnectionRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get connections by status
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectionsByStatus")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<NetworkConnection> getConnectionsByStatus(ConnectionStatus status) {
        logger.debug("Fetching network connections with status: {}", status);
        return networkConnectionRepository.findByConnectionStatus(status);
    }

    /**
     * Get connected connections
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectedConnections")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<NetworkConnection> getConnectedConnections() {
        logger.debug("Fetching connected network connections");
        return networkConnectionRepository.findConnectedConnections();
    }

    /**
     * Get connections requiring attention
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetConnectionsRequiringAttention")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<NetworkConnection> getConnectionsRequiringAttention() {
        logger.debug("Fetching network connections requiring attention");
        return networkConnectionRepository.findConnectionsRequiringAttention();
    }

    /**
     * Update connection status
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateConnectionStatus")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public NetworkConnection updateConnectionStatus(Long id, ConnectionStatus status) {
        logger.info("Updating connection status for ID: {} to {}", id, status);
        
        return networkConnectionRepository.findById(id)
                .map(connection -> {
                    ConnectionStatus oldStatus = connection.getConnectionStatus();
                    connection.setConnectionStatus(status);
                    
                    // Update timestamps based on status change
                    if (status == ConnectionStatus.CONNECTED && oldStatus != ConnectionStatus.CONNECTED) {
                        connection.setLastConnected(LocalDateTime.now());
                    } else if (status == ConnectionStatus.DISCONNECTED && oldStatus == ConnectionStatus.CONNECTED) {
                        connection.setLastDisconnected(LocalDateTime.now());
                        // Calculate uptime
                        if (connection.getLastConnected() != null) {
                            long uptimeMinutes = java.time.Duration.between(
                                connection.getLastConnected(), LocalDateTime.now()).toMinutes();
                            connection.setConnectionUptimeHours(uptimeMinutes / 60.0);
                        }
                    }
                    
                    return networkConnectionRepository.save(connection);
                })
                .orElseThrow(() -> new IllegalArgumentException("Network connection not found with ID: " + id));
    }

    /**
     * Update connection diagnostics
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateConnectionDiagnostics")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public NetworkConnection updateConnectionDiagnostics(Long id, Integer signalStrength, 
                                                        Integer pingResponse, Double packetLoss) {
        logger.debug("Updating connection diagnostics for ID: {}", id);
        
        return networkConnectionRepository.findById(id)
                .map(connection -> {
                    if (signalStrength != null) {
                        connection.setSignalStrength(signalStrength);
                    }
                    if (pingResponse != null) {
                        connection.setLastPingResponseMs(pingResponse);
                    }
                    if (packetLoss != null) {
                        connection.setPacketLossPercentage(packetLoss);
                    }
                    
                    return networkConnectionRepository.save(connection);
                })
                .orElseThrow(() -> new IllegalArgumentException("Network connection not found with ID: " + id));
    }

    /**
     * Increment connection error count
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackIncrementErrorCount")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public NetworkConnection incrementErrorCount(Long id) {
        logger.debug("Incrementing error count for connection ID: {}", id);
        
        return networkConnectionRepository.findById(id)
                .map(connection -> {
                    int currentCount = connection.getConnectionErrorsCount() != null ? 
                                     connection.getConnectionErrorsCount() : 0;
                    connection.setConnectionErrorsCount(currentCount + 1);
                    return networkConnectionRepository.save(connection);
                })
                .orElseThrow(() -> new IllegalArgumentException("Network connection not found with ID: " + id));
    }

    /**
     * Get network connection statistics
     */
    @Cacheable(value = "networkStatistics", unless = "#result.containsKey('error')")
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetNetworkStatistics")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Map<String, Object> getNetworkStatistics() {
        logger.debug("Generating network connection statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Connection status statistics
        List<Object[]> statusStats = networkConnectionRepository.getConnectionStatusStatistics();
        Map<ConnectionStatus, Long> statusMap = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (ConnectionStatus) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("connectionStatusCounts", statusMap);
        
        // Connection type statistics
        List<Object[]> typeStats = networkConnectionRepository.getConnectionTypeStatistics();
        Map<ConnectionType, Long> typeMap = typeStats.stream()
                .collect(Collectors.toMap(
                        row -> (ConnectionType) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("connectionTypeCounts", typeMap);
        
        // Health metrics
        List<NetworkConnection> allConnections = networkConnectionRepository.findAll();
        long totalConnections = allConnections.size();
        long connectedCount = allConnections.stream()
                .mapToLong(conn -> conn.isConnected() ? 1 : 0)
                .sum();
        long strongSignalCount = allConnections.stream()
                .mapToLong(conn -> conn.hasStrongSignal() ? 1 : 0)
                .sum();
        long goodLatencyCount = allConnections.stream()
                .mapToLong(conn -> conn.hasGoodLatency() ? 1 : 0)
                .sum();
        
        statistics.put("totalConnections", totalConnections);
        statistics.put("connectedCount", connectedCount);
        statistics.put("connectionRate", totalConnections > 0 ? (double) connectedCount / totalConnections * 100 : 0);
        statistics.put("strongSignalCount", strongSignalCount);
        statistics.put("goodLatencyCount", goodLatencyCount);
        
        // Duplicate detection
        List<Object[]> duplicateMachineIds = networkConnectionRepository.findDuplicateMachineIds();
        List<Object[]> duplicateMacAddresses = networkConnectionRepository.findDuplicateMacAddresses();
        List<Object[]> duplicateIpAddresses = networkConnectionRepository.findDuplicateIpAddresses();
        
        statistics.put("duplicateMachineIds", duplicateMachineIds.size());
        statistics.put("duplicateMacAddresses", duplicateMacAddresses.size());
        statistics.put("duplicateIpAddresses", duplicateIpAddresses.size());
        
        return statistics;
    }

    /**
     * Detect network issues
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackDetectNetworkIssues")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<Map<String, Object>> detectNetworkIssues() {
        logger.debug("Detecting network issues");
        
        List<Map<String, Object>> issues = new ArrayList<>();
        
        // Check for duplicate machine IDs
        List<Object[]> duplicateMachineIds = networkConnectionRepository.findDuplicateMachineIds();
        for (Object[] duplicate : duplicateMachineIds) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "DUPLICATE_MACHINE_ID");
            issue.put("machineId", duplicate[0]);
            issue.put("count", duplicate[1]);
            issue.put("severity", "HIGH");
            issues.add(issue);
        }
        
        // Check for duplicate MAC addresses
        List<Object[]> duplicateMacAddresses = networkConnectionRepository.findDuplicateMacAddresses();
        for (Object[] duplicate : duplicateMacAddresses) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "DUPLICATE_MAC_ADDRESS");
            issue.put("macAddress", duplicate[0]);
            issue.put("count", duplicate[1]);
            issue.put("severity", "CRITICAL");
            issues.add(issue);
        }
        
        // Check for connections requiring attention
        List<NetworkConnection> problematicConnections = networkConnectionRepository.findConnectionsRequiringAttention();
        for (NetworkConnection connection : problematicConnections) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "CONNECTION_ISSUE");
            issue.put("machineId", connection.getMachineId());
            issue.put("status", connection.getConnectionStatus());
            issue.put("signalStrength", connection.getSignalStrength());
            issue.put("latency", connection.getLastPingResponseMs());
            issue.put("packetLoss", connection.getPacketLossPercentage());
            issue.put("severity", "MEDIUM");
            issues.add(issue);
        }
        
        return issues;
    }

    // Fallback methods
    public NetworkConnection fallbackCreateConnection(NetworkConnection connection, Exception ex) {
        logger.error("Fallback: Failed to create network connection", ex);
        throw new RuntimeException("Network connection creation service temporarily unavailable");
    }

    public Optional<NetworkConnection> fallbackGetConnection(Long id, Exception ex) {
        logger.error("Fallback: Failed to get network connection by ID", ex);
        return Optional.empty();
    }

    public Optional<NetworkConnection> fallbackGetConnectionByMachineId(String machineId, Exception ex) {
        logger.error("Fallback: Failed to get network connection by machine ID", ex);
        return Optional.empty();
    }

    public Optional<NetworkConnection> fallbackGetConnectionByMacAddress(String macAddress, Exception ex) {
        logger.error("Fallback: Failed to get network connection by MAC address", ex);
        return Optional.empty();
    }

    public List<NetworkConnection> fallbackGetAllConnections(Exception ex) {
        logger.error("Fallback: Failed to get all network connections", ex);
        return Collections.emptyList();
    }

    public List<NetworkConnection> fallbackGetConnectionsByEquipment(Long equipmentId, Exception ex) {
        logger.error("Fallback: Failed to get connections by equipment", ex);
        return Collections.emptyList();
    }

    public List<NetworkConnection> fallbackGetConnectionsByStatus(ConnectionStatus status, Exception ex) {
        logger.error("Fallback: Failed to get connections by status", ex);
        return Collections.emptyList();
    }

    public List<NetworkConnection> fallbackGetConnectedConnections(Exception ex) {
        logger.error("Fallback: Failed to get connected connections", ex);
        return Collections.emptyList();
    }

    public List<NetworkConnection> fallbackGetConnectionsRequiringAttention(Exception ex) {
        logger.error("Fallback: Failed to get connections requiring attention", ex);
        return Collections.emptyList();
    }

    public NetworkConnection fallbackUpdateConnectionStatus(Long id, ConnectionStatus status, Exception ex) {
        logger.error("Fallback: Failed to update connection status", ex);
        throw new RuntimeException("Connection status update service temporarily unavailable");
    }

    public NetworkConnection fallbackUpdateConnectionDiagnostics(Long id, Integer signalStrength, 
                                                               Integer pingResponse, Double packetLoss, Exception ex) {
        logger.error("Fallback: Failed to update connection diagnostics", ex);
        throw new RuntimeException("Connection diagnostics update service temporarily unavailable");
    }

    public NetworkConnection fallbackIncrementErrorCount(Long id, Exception ex) {
        logger.error("Fallback: Failed to increment error count", ex);
        throw new RuntimeException("Error count increment service temporarily unavailable");
    }

    public Map<String, Object> fallbackGetNetworkStatistics(Exception ex) {
        logger.error("Fallback: Failed to get network statistics", ex);
        Map<String, Object> fallbackStats = new HashMap<>();
        fallbackStats.put("error", "Network statistics service temporarily unavailable");
        fallbackStats.put("timestamp", LocalDateTime.now());
        return fallbackStats;
    }

    public List<Map<String, Object>> fallbackDetectNetworkIssues(Exception ex) {
        logger.error("Fallback: Failed to detect network issues", ex);
        return Collections.emptyList();
    }

    /**
     * Perform network connectivity test
     */
    @CircuitBreaker(name = "network", fallbackMethod = "fallbackConnectivityTest")
    @RateLimiter(name = "api")
    @Retry(name = "network")
    public Map<String, Object> performConnectivityTest(String machineId) {
        logger.info("Performing connectivity test for machine ID: {}", machineId);

        Optional<NetworkConnection> connectionOpt = networkConnectionRepository.findByMachineId(machineId);
        if (connectionOpt.isEmpty()) {
            throw new IllegalArgumentException("Network connection not found for machine ID: " + machineId);
        }

        NetworkConnection connection = connectionOpt.get();
        Map<String, Object> testResults = new HashMap<>();

        // Simulate connectivity test (in real implementation, this would perform actual network tests)
        testResults.put("machineId", machineId);
        testResults.put("ipAddress", connection.getIpAddress());
        testResults.put("macAddress", connection.getMacAddress());
        testResults.put("connectionStatus", connection.getConnectionStatus());
        testResults.put("testTimestamp", LocalDateTime.now());

        // Simulate ping test
        boolean pingSuccess = connection.getConnectionStatus() == ConnectionStatus.CONNECTED;
        testResults.put("pingSuccess", pingSuccess);
        testResults.put("pingResponseMs", pingSuccess ?
            (connection.getLastPingResponseMs() != null ? connection.getLastPingResponseMs() : 50) : null);

        // Simulate bandwidth test
        testResults.put("bandwidthMbps", connection.getBandwidthMbps());
        testResults.put("signalStrength", connection.getSignalStrength());
        testResults.put("packetLoss", connection.getPacketLossPercentage());

        // Overall health assessment
        boolean healthy = pingSuccess &&
                         (connection.getSignalStrength() == null || connection.getSignalStrength() > -70) &&
                         (connection.getPacketLossPercentage() == null || connection.getPacketLossPercentage() < 5.0);
        testResults.put("overallHealth", healthy ? "HEALTHY" : "DEGRADED");

        return testResults;
    }

    public Map<String, Object> fallbackConnectivityTest(String machineId, Exception ex) {
        logger.error("Fallback: Failed to perform connectivity test for machine ID: {}", machineId, ex);
        Map<String, Object> fallbackResult = new HashMap<>();
        fallbackResult.put("error", "Connectivity test service temporarily unavailable");
        fallbackResult.put("machineId", machineId);
        fallbackResult.put("timestamp", LocalDateTime.now());
        return fallbackResult;
    }
}
