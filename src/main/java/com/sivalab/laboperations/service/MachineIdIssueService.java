package com.sivalab.laboperations.service;

import com.sivalab.laboperations.config.NetworkConfig;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.MachineIdIssueRepository;
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
 * Service for managing machine ID issues with fault tolerance
 * This service is optional and only enabled when network monitoring is configured
 */
@Service
@Transactional
@ConditionalOnProperty(name = "lab.network.enabled", havingValue = "true", matchIfMissing = false)
public class MachineIdIssueService {

    private static final Logger logger = LoggerFactory.getLogger(MachineIdIssueService.class);

    @Autowired
    private MachineIdIssueRepository machineIdIssueRepository;

    @Autowired
    private NetworkConnectionRepository networkConnectionRepository;

    @Autowired
    private LabEquipmentRepository equipmentRepository;

    @Autowired
    private NetworkConfig networkConfig;

    /**
     * Create new machine ID issue
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCreateIssue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public MachineIdIssue createIssue(MachineIdIssue issue) {
        logger.info("Creating new machine ID issue: {}", issue.getTitle());
        
        issue.setFirstDetected(LocalDateTime.now());
        issue.setLastOccurrence(LocalDateTime.now());
        
        return machineIdIssueRepository.save(issue);
    }

    /**
     * Get issue by ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetIssue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<MachineIdIssue> getIssueById(Long id) {
        logger.debug("Fetching machine ID issue with ID: {}", id);
        return machineIdIssueRepository.findById(id);
    }

    /**
     * Get all issues
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetAllIssues")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> getAllIssues() {
        logger.debug("Fetching all machine ID issues");
        return machineIdIssueRepository.findAll();
    }

    /**
     * Get open issues
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetOpenIssues")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> getOpenIssues() {
        logger.debug("Fetching open machine ID issues");
        return machineIdIssueRepository.findOpenIssues();
    }

    /**
     * Get issues by equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetIssuesByEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> getIssuesByEquipment(Long equipmentId) {
        logger.debug("Fetching machine ID issues for equipment ID: {}", equipmentId);
        return machineIdIssueRepository.findByEquipmentId(equipmentId);
    }

    /**
     * Get issues by severity
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetIssuesBySeverity")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> getIssuesBySeverity(IssueSeverity severity) {
        logger.debug("Fetching machine ID issues with severity: {}", severity);
        return machineIdIssueRepository.findBySeverityOrderByCreatedAtDesc(severity);
    }

    /**
     * Get high priority issues
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetHighPriorityIssues")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> getHighPriorityIssues() {
        logger.debug("Fetching high priority machine ID issues");
        return machineIdIssueRepository.findHighPriorityIssues();
    }

    /**
     * Update issue status
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateIssueStatus")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public MachineIdIssue updateIssueStatus(Long id, IssueStatus status, String resolvedBy) {
        logger.info("Updating issue status for ID: {} to {}", id, status);
        
        return machineIdIssueRepository.findById(id)
                .map(issue -> {
                    issue.setStatus(status);
                    
                    if (status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED) {
                        issue.setResolvedAt(LocalDateTime.now());
                        issue.setResolvedBy(resolvedBy);
                        
                        // Calculate actual resolution time
                        if (issue.getCreatedAt() != null) {
                            long resolutionMinutes = java.time.Duration.between(
                                issue.getCreatedAt(), LocalDateTime.now()).toMinutes();
                            issue.setActualResolutionTimeHours(resolutionMinutes / 60.0);
                        }
                    }
                    
                    return machineIdIssueRepository.save(issue);
                })
                .orElseThrow(() -> new IllegalArgumentException("Machine ID issue not found with ID: " + id));
    }

    /**
     * Assign issue to technician
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackAssignIssue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public MachineIdIssue assignIssue(Long id, String assignedTo) {
        logger.info("Assigning issue ID: {} to {}", id, assignedTo);
        
        return machineIdIssueRepository.findById(id)
                .map(issue -> {
                    issue.setAssignedTo(assignedTo);
                    if (issue.getStatus() == IssueStatus.OPEN) {
                        issue.setStatus(IssueStatus.IN_PROGRESS);
                    }
                    return machineIdIssueRepository.save(issue);
                })
                .orElseThrow(() -> new IllegalArgumentException("Machine ID issue not found with ID: " + id));
    }

    /**
     * Escalate issue
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackEscalateIssue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public MachineIdIssue escalateIssue(Long id, String escalatedTo) {
        logger.info("Escalating issue ID: {} to {}", id, escalatedTo);
        
        return machineIdIssueRepository.findById(id)
                .map(issue -> {
                    issue.setEscalated(true);
                    issue.setEscalatedTo(escalatedTo);
                    issue.setEscalatedAt(LocalDateTime.now());
                    issue.setStatus(IssueStatus.ESCALATED);
                    issue.setPriorityLevel(1); // Set to high priority
                    return machineIdIssueRepository.save(issue);
                })
                .orElseThrow(() -> new IllegalArgumentException("Machine ID issue not found with ID: " + id));
    }

    /**
     * Record issue occurrence
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackRecordOccurrence")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public MachineIdIssue recordIssueOccurrence(Long id) {
        logger.debug("Recording occurrence for issue ID: {}", id);
        
        return machineIdIssueRepository.findById(id)
                .map(issue -> {
                    issue.incrementOccurrenceCount();
                    return machineIdIssueRepository.save(issue);
                })
                .orElseThrow(() -> new IllegalArgumentException("Machine ID issue not found with ID: " + id));
    }

    /**
     * Auto-detect machine ID issues
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackAutoDetectIssues")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<MachineIdIssue> autoDetectIssues() {
        logger.info("Auto-detecting machine ID issues");
        
        List<MachineIdIssue> detectedIssues = new ArrayList<>();
        
        // Detect duplicate machine IDs
        List<Object[]> duplicateMachineIds = networkConnectionRepository.findDuplicateMachineIds();
        for (Object[] duplicate : duplicateMachineIds) {
            String machineId = (String) duplicate[0];
            Long count = (Long) duplicate[1];
            
            // Check if issue already exists
            List<MachineIdIssue> existingIssues = machineIdIssueRepository.findByMachineIdCurrent(machineId);
            boolean hasOpenIssue = existingIssues.stream().anyMatch(MachineIdIssue::isOpen);
            
            if (!hasOpenIssue) {
                Optional<NetworkConnection> connectionOpt = networkConnectionRepository.findByMachineId(machineId);
                if (connectionOpt.isPresent()) {
                    NetworkConnection connection = connectionOpt.get();
                    MachineIdIssue issue = new MachineIdIssue();
                    issue.setEquipment(connection.getEquipment());
                    issue.setNetworkConnection(connection);
                    issue.setTitle("Duplicate Machine ID Detected");
                    issue.setDescription("Machine ID " + machineId + " is used by " + count + " devices");
                    issue.setIssueType(IssueType.MACHINE_ID_DUPLICATE);
                    issue.setSeverity(IssueSeverity.HIGH);
                    issue.setMachineIdCurrent(machineId);
                    issue.setAutoDetected(true);
                    issue.setPriorityLevel(1);
                    
                    detectedIssues.add(machineIdIssueRepository.save(issue));
                }
            }
        }
        
        // Detect MAC address conflicts
        List<Object[]> duplicateMacAddresses = networkConnectionRepository.findDuplicateMacAddresses();
        for (Object[] duplicate : duplicateMacAddresses) {
            String macAddress = (String) duplicate[0];
            Long count = (Long) duplicate[1];
            
            Optional<NetworkConnection> connectionOpt = networkConnectionRepository.findByMacAddress(macAddress);
            if (connectionOpt.isPresent()) {
                NetworkConnection connection = connectionOpt.get();
                // Check if issue already exists
                List<MachineIdIssue> existingIssues = machineIdIssueRepository.findByMacAddressCurrent(macAddress);
                boolean hasOpenIssue = existingIssues.stream().anyMatch(MachineIdIssue::isOpen);

                if (!hasOpenIssue) {
                    MachineIdIssue issue = new MachineIdIssue();
                    issue.setEquipment(connection.getEquipment());
                    issue.setNetworkConnection(connection);
                    issue.setTitle("MAC Address Conflict Detected");
                    issue.setDescription("MAC address " + macAddress + " is used by " + count + " devices");
                    issue.setIssueType(IssueType.MAC_ADDRESS_CONFLICT);
                    issue.setSeverity(IssueSeverity.CRITICAL);
                    issue.setMacAddressCurrent(macAddress);
                    issue.setAutoDetected(true);
                    issue.setPriorityLevel(1);
                    
                    detectedIssues.add(machineIdIssueRepository.save(issue));
                }
            }
        }
        
        // Detect connection issues
        List<NetworkConnection> problematicConnections = networkConnectionRepository.findConnectionsRequiringAttention();
        for (NetworkConnection connection : problematicConnections) {
            // Check if issue already exists for this connection
            List<MachineIdIssue> existingIssues = machineIdIssueRepository.findByNetworkConnection(connection);
            boolean hasOpenConnectionIssue = existingIssues.stream()
                    .anyMatch(issue -> issue.isOpen() && 
                             (issue.getIssueType() == IssueType.CONNECTION_TIMEOUT ||
                              issue.getIssueType() == IssueType.SIGNAL_INTERFERENCE ||
                              issue.getIssueType() == IssueType.PACKET_LOSS_HIGH));
            
            if (!hasOpenConnectionIssue) {
                MachineIdIssue issue = new MachineIdIssue();
                issue.setEquipment(connection.getEquipment());
                issue.setNetworkConnection(connection);
                issue.setTitle("Network Connection Issue Detected");
                issue.setDescription("Connection issues detected for machine " + connection.getMachineId());
                
                // Determine specific issue type based on connection status
                if (connection.getConnectionStatus() == ConnectionStatus.TIMEOUT) {
                    issue.setIssueType(IssueType.CONNECTION_TIMEOUT);
                } else if (connection.getSignalStrength() != null && connection.getSignalStrength() < -70) {
                    issue.setIssueType(IssueType.SIGNAL_INTERFERENCE);
                } else if (connection.getPacketLossPercentage() != null && connection.getPacketLossPercentage() > 5.0) {
                    issue.setIssueType(IssueType.PACKET_LOSS_HIGH);
                } else {
                    issue.setIssueType(IssueType.OTHER);
                }
                
                issue.setSeverity(IssueSeverity.MEDIUM);
                issue.setMachineIdCurrent(connection.getMachineId());
                issue.setMacAddressCurrent(connection.getMacAddress());
                issue.setIpAddressCurrent(connection.getIpAddress());
                issue.setAutoDetected(true);
                issue.setPriorityLevel(2);
                
                detectedIssues.add(machineIdIssueRepository.save(issue));
            }
        }
        
        logger.info("Auto-detected {} machine ID issues", detectedIssues.size());
        return detectedIssues;
    }

    /**
     * Get machine ID issue statistics
     */
    @Cacheable(value = "machineIdIssueStatistics", unless = "#result.containsKey('error')")
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetIssueStatistics")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Map<String, Object> getIssueStatistics() {
        logger.debug("Generating machine ID issue statistics");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Issue type statistics
        List<Object[]> typeStats = machineIdIssueRepository.getIssueTypeStatistics();
        Map<IssueType, Long> typeMap = typeStats.stream()
                .collect(Collectors.toMap(
                        row -> (IssueType) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("issueTypeCounts", typeMap);
        
        // Issue severity statistics
        List<Object[]> severityStats = machineIdIssueRepository.getIssueSeverityStatistics();
        Map<IssueSeverity, Long> severityMap = severityStats.stream()
                .collect(Collectors.toMap(
                        row -> (IssueSeverity) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("issueSeverityCounts", severityMap);
        
        // Issue status statistics
        List<Object[]> statusStats = machineIdIssueRepository.getIssueStatusStatistics();
        Map<IssueStatus, Long> statusMap = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (IssueStatus) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("issueStatusCounts", statusMap);
        
        // Summary counts
        Long totalIssues = machineIdIssueRepository.count();
        Long openIssues = machineIdIssueRepository.countOpenIssues();
        Long resolvedIssues = machineIdIssueRepository.countResolvedIssues();
        Long criticalIssues = machineIdIssueRepository.countBySeverity(IssueSeverity.CRITICAL);
        Long escalatedIssues = machineIdIssueRepository.countByEscalatedTrue();
        
        statistics.put("totalIssues", totalIssues);
        statistics.put("openIssues", openIssues);
        statistics.put("resolvedIssues", resolvedIssues);
        statistics.put("criticalIssues", criticalIssues);
        statistics.put("escalatedIssues", escalatedIssues);
        
        // Resolution rate
        double resolutionRate = totalIssues > 0 ? (double) resolvedIssues / totalIssues * 100 : 0;
        statistics.put("resolutionRate", resolutionRate);
        
        return statistics;
    }

    // Fallback methods
    public MachineIdIssue fallbackCreateIssue(MachineIdIssue issue, Exception ex) {
        logger.error("Fallback: Failed to create machine ID issue", ex);
        throw new RuntimeException("Machine ID issue creation service temporarily unavailable");
    }

    public Optional<MachineIdIssue> fallbackGetIssue(Long id, Exception ex) {
        logger.error("Fallback: Failed to get machine ID issue by ID", ex);
        return Optional.empty();
    }

    public List<MachineIdIssue> fallbackGetAllIssues(Exception ex) {
        logger.error("Fallback: Failed to get all machine ID issues", ex);
        return Collections.emptyList();
    }

    public List<MachineIdIssue> fallbackGetOpenIssues(Exception ex) {
        logger.error("Fallback: Failed to get open machine ID issues", ex);
        return Collections.emptyList();
    }

    public List<MachineIdIssue> fallbackGetIssuesByEquipment(Long equipmentId, Exception ex) {
        logger.error("Fallback: Failed to get issues by equipment", ex);
        return Collections.emptyList();
    }

    public List<MachineIdIssue> fallbackGetIssuesBySeverity(IssueSeverity severity, Exception ex) {
        logger.error("Fallback: Failed to get issues by severity", ex);
        return Collections.emptyList();
    }

    public List<MachineIdIssue> fallbackGetHighPriorityIssues(Exception ex) {
        logger.error("Fallback: Failed to get high priority issues", ex);
        return Collections.emptyList();
    }

    public MachineIdIssue fallbackUpdateIssueStatus(Long id, IssueStatus status, String resolvedBy, Exception ex) {
        logger.error("Fallback: Failed to update issue status", ex);
        throw new RuntimeException("Issue status update service temporarily unavailable");
    }

    public MachineIdIssue fallbackAssignIssue(Long id, String assignedTo, Exception ex) {
        logger.error("Fallback: Failed to assign issue", ex);
        throw new RuntimeException("Issue assignment service temporarily unavailable");
    }

    public MachineIdIssue fallbackEscalateIssue(Long id, String escalatedTo, Exception ex) {
        logger.error("Fallback: Failed to escalate issue", ex);
        throw new RuntimeException("Issue escalation service temporarily unavailable");
    }

    public MachineIdIssue fallbackRecordOccurrence(Long id, Exception ex) {
        logger.error("Fallback: Failed to record issue occurrence", ex);
        throw new RuntimeException("Issue occurrence recording service temporarily unavailable");
    }

    public List<MachineIdIssue> fallbackAutoDetectIssues(Exception ex) {
        logger.error("Fallback: Failed to auto-detect issues", ex);
        return Collections.emptyList();
    }

    public Map<String, Object> fallbackGetIssueStatistics(Exception ex) {
        logger.error("Fallback: Failed to get issue statistics", ex);
        Map<String, Object> fallbackStats = new HashMap<>();
        fallbackStats.put("error", "Issue statistics service temporarily unavailable");
        fallbackStats.put("timestamp", LocalDateTime.now());
        return fallbackStats;
    }
}
