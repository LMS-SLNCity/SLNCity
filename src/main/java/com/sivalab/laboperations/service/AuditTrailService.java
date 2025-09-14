package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.AuditAction;
import com.sivalab.laboperations.entity.AuditTrail;
import com.sivalab.laboperations.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Audit Trail Management
 * Provides comprehensive audit trail operations and statistics
 */
@Service
@Transactional
public class AuditTrailService {
    
    private final AuditTrailRepository auditTrailRepository;
    
    @Autowired
    public AuditTrailService(AuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }
    
    /**
     * Get all audit trail entries with pagination
     */
    public Page<AuditTrail> getAllAuditTrailEntries(Pageable pageable) {
        return auditTrailRepository.findAllByOrderByTimestampDesc(pageable);
    }
    
    /**
     * Get audit trail entries by table name
     */
    public Page<AuditTrail> getAuditTrailByTable(String tableName, Pageable pageable) {
        return auditTrailRepository.findByTableNameOrderByTimestampDesc(tableName, pageable);
    }
    
    /**
     * Get audit trail entries by user
     */
    public Page<AuditTrail> getAuditTrailByUser(String userId, Pageable pageable) {
        return auditTrailRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
    
    /**
     * Get audit trail entries by action
     */
    public Page<AuditTrail> getAuditTrailByAction(String action, Pageable pageable) {
        return auditTrailRepository.findByActionOrderByTimestampDesc(action, pageable);
    }
    
    /**
     * Get audit trail entries since a specific date
     */
    public Page<AuditTrail> getAuditTrailSince(LocalDateTime since, Pageable pageable) {
        return auditTrailRepository.findByTimestampAfterOrderByTimestampDesc(since, pageable);
    }
    
    /**
     * Get suspicious activities
     */
    public List<AuditTrail> getSuspiciousActivities(LocalDateTime since) {
        return auditTrailRepository.findSuspiciousActivities(since);
    }
    
    /**
     * Get audit trail statistics
     */
    public Map<String, Object> getAuditTrailStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
            
            // Get basic statistics
            Object[] auditStats = auditTrailRepository.getAuditStatistics(last24Hours);
            
            if (auditStats != null && auditStats.length >= 6) {
                stats.put("totalRecords", auditStats[0]);
                stats.put("uniqueUsers", auditStats[1]);
                stats.put("tablesAffected", auditStats[2]);
                stats.put("criticalEvents", auditStats[3]);
                stats.put("highSeverityEvents", auditStats[4]);
                stats.put("recentEvents", auditStats[5]);
            } else {
                // Fallback to individual queries
                long totalRecords = auditTrailRepository.count();
                long recentEvents = auditTrailRepository.countByTimestampAfter(last24Hours);
                long weeklyEvents = auditTrailRepository.countByTimestampAfter(lastWeek);
                
                stats.put("totalRecords", totalRecords);
                stats.put("recentEvents", recentEvents);
                stats.put("weeklyEvents", weeklyEvents);
                stats.put("uniqueUsers", 0L);
                stats.put("tablesAffected", 0L);
                stats.put("criticalEvents", 0L);
                stats.put("highSeverityEvents", 0L);
            }
            
            // Get suspicious activities count
            List<AuditTrail> suspiciousActivities = getSuspiciousActivities(last24Hours);
            stats.put("suspiciousActivities", suspiciousActivities.size());
            
            stats.put("timestamp", LocalDateTime.now());
            stats.put("status", "success");
            
        } catch (Exception e) {
            stats.put("error", "Failed to retrieve audit statistics: " + e.getMessage());
            stats.put("status", "error");
            stats.put("timestamp", LocalDateTime.now());
        }
        
        return stats;
    }
    
    /**
     * Create audit trail entry
     */
    public AuditTrail createAuditTrailEntry(String tableName, String action, String userId,
                                          String description, String severity) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setTableName(tableName);
        // Convert string action to AuditAction enum
        try {
            auditTrail.setAction(AuditAction.valueOf(action.toUpperCase()));
        } catch (IllegalArgumentException e) {
            auditTrail.setAction(AuditAction.ACCESS); // Default fallback
        }
        auditTrail.setUserId(userId);
        auditTrail.setDescription(description);
        auditTrail.setSeverity(severity);
        auditTrail.setTimestamp(LocalDateTime.now());
        
        return auditTrailRepository.save(auditTrail);
    }
    
    /**
     * Log user action for audit trail
     */
    public void logUserAction(String tableName, String action, String userId, String description) {
        createAuditTrailEntry(tableName, action, userId, description, "INFO");
    }
    
    /**
     * Log critical action for audit trail
     */
    public void logCriticalAction(String tableName, String action, String userId, String description) {
        createAuditTrailEntry(tableName, action, userId, description, "CRITICAL");
    }
    
    /**
     * Clean up old audit records (for maintenance)
     */
    public long cleanupOldAuditRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        long recordsToDelete = auditTrailRepository.countByTimestampBefore(cutoffDate);
        auditTrailRepository.deleteByTimestampBefore(cutoffDate);
        return recordsToDelete;
    }
}
