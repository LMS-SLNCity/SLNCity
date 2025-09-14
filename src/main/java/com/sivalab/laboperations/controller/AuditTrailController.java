package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.AuditTrail;
import com.sivalab.laboperations.service.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Audit Trail Management
 * Provides endpoints for audit trail operations and history tracking
 */
@RestController
@RequestMapping("/audit-trail")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditTrailController {
    
    private final AuditTrailService auditTrailService;
    
    @Autowired
    public AuditTrailController(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }
    
    /**
     * Get all audit trail entries
     * GET /audit-trail
     */
    @GetMapping
    public ResponseEntity<List<AuditTrail>> getAllAuditTrailEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditTrail> auditPage = auditTrailService.getAllAuditTrailEntries(pageable);
            return ResponseEntity.ok(auditPage.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit trail entries: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit trail entries by table name
     * GET /audit-trail/table/{tableName}
     */
    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<AuditTrail>> getAuditTrailByTable(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditTrail> auditPage = auditTrailService.getAuditTrailByTable(tableName, pageable);
            return ResponseEntity.ok(auditPage.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit trail for table: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit trail entries by user
     * GET /audit-trail/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditTrail>> getAuditTrailByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditTrail> auditPage = auditTrailService.getAuditTrailByUser(userId, pageable);
            return ResponseEntity.ok(auditPage.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit trail for user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit trail entries by action
     * GET /audit-trail/action/{action}
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditTrail>> getAuditTrailByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditTrail> auditPage = auditTrailService.getAuditTrailByAction(action, pageable);
            return ResponseEntity.ok(auditPage.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit trail for action: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit trail statistics
     * GET /audit-trail/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAuditTrailStatistics() {
        try {
            Map<String, Object> statistics = auditTrailService.getAuditTrailStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit trail statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent audit trail entries (last 24 hours)
     * GET /audit-trail/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AuditTrail>> getRecentAuditTrailEntries(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditTrail> auditPage = auditTrailService.getAuditTrailSince(since, pageable);
            return ResponseEntity.ok(auditPage.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve recent audit trail entries: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get suspicious activities
     * GET /audit-trail/suspicious
     */
    @GetMapping("/suspicious")
    public ResponseEntity<List<AuditTrail>> getSuspiciousActivities(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<AuditTrail> suspiciousActivities = auditTrailService.getSuspiciousActivities(since);
            return ResponseEntity.ok(suspiciousActivities);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve suspicious activities: " + e.getMessage(), e);
        }
    }
}
