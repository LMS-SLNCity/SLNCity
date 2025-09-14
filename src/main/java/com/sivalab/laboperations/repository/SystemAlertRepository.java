package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.SystemAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SystemAlert entity
 */
@Repository
public interface SystemAlertRepository extends JpaRepository<SystemAlert, Long> {

    /**
     * Find active alerts
     */
    Page<SystemAlert> findByStatusOrderByTriggeredAtDesc(String status, Pageable pageable);

    /**
     * Find alerts by type
     */
    Page<SystemAlert> findByAlertTypeOrderByTriggeredAtDesc(String alertType, Pageable pageable);

    /**
     * Find alerts by severity
     */
    Page<SystemAlert> findBySeverityOrderByTriggeredAtDesc(String severity, Pageable pageable);

    /**
     * Find alerts by source module
     */
    Page<SystemAlert> findBySourceModuleOrderByTriggeredAtDesc(String sourceModule, Pageable pageable);

    /**
     * Find alerts within a date range
     */
    Page<SystemAlert> findByTriggeredAtBetweenOrderByTriggeredAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find unacknowledged alerts
     */
    @Query("SELECT a FROM SystemAlert a WHERE a.acknowledgedAt IS NULL AND a.status = 'ACTIVE' ORDER BY a.triggeredAt DESC")
    List<SystemAlert> findUnacknowledgedAlerts();

    /**
     * Find unresolved alerts
     */
    @Query("SELECT a FROM SystemAlert a WHERE a.resolvedAt IS NULL AND a.status = 'ACTIVE' ORDER BY a.triggeredAt DESC")
    List<SystemAlert> findUnresolvedAlerts();

    /**
     * Find critical alerts
     */
    @Query("SELECT a FROM SystemAlert a WHERE a.severity = 'CRITICAL' AND a.status = 'ACTIVE' ORDER BY a.triggeredAt DESC")
    List<SystemAlert> findCriticalAlerts();

    /**
     * Find alerts by alert code
     */
    List<SystemAlert> findByAlertCodeOrderByTriggeredAtDesc(String alertCode);

    /**
     * Find alerts for a specific source record
     */
    List<SystemAlert> findBySourceTableAndSourceRecordIdOrderByTriggeredAtDesc(String sourceTable, Long sourceRecordId);

    /**
     * Count active alerts by severity
     */
    @Query("SELECT a.severity, COUNT(a) FROM SystemAlert a WHERE a.status = 'ACTIVE' GROUP BY a.severity")
    List<Object[]> countActiveBySeverity();

    /**
     * Count alerts by type
     */
    @Query("SELECT a.alertType, COUNT(a) FROM SystemAlert a GROUP BY a.alertType")
    List<Object[]> countByType();

    /**
     * Count alerts by status
     */
    @Query("SELECT a.status, COUNT(a) FROM SystemAlert a GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * Find alerts by multiple criteria
     */
    @Query("SELECT a FROM SystemAlert a WHERE " +
           "(:alertType IS NULL OR a.alertType = :alertType) AND " +
           "(:alertCode IS NULL OR a.alertCode = :alertCode) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:sourceModule IS NULL OR a.sourceModule = :sourceModule) AND " +
           "(:triggeredBy IS NULL OR a.triggeredBy = :triggeredBy) AND " +
           "(:startDate IS NULL OR a.triggeredAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.triggeredAt <= :endDate) " +
           "ORDER BY a.triggeredAt DESC")
    Page<SystemAlert> findByCriteria(
            @Param("alertType") String alertType,
            @Param("alertCode") String alertCode,
            @Param("severity") String severity,
            @Param("status") String status,
            @Param("sourceModule") String sourceModule,
            @Param("triggeredBy") String triggeredBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get alert statistics
     */
    @Query("SELECT " +
           "COUNT(a) as totalAlerts, " +
           "COUNT(CASE WHEN a.status = 'ACTIVE' THEN 1 END) as activeAlerts, " +
           "COUNT(CASE WHEN a.status = 'RESOLVED' THEN 1 END) as resolvedAlerts, " +
           "COUNT(CASE WHEN a.acknowledgedAt IS NULL AND a.status = 'ACTIVE' THEN 1 END) as unacknowledgedAlerts, " +
           "COUNT(CASE WHEN a.severity = 'CRITICAL' THEN 1 END) as criticalAlerts, " +
           "COUNT(CASE WHEN a.severity = 'HIGH' THEN 1 END) as highSeverityAlerts, " +
           "COUNT(CASE WHEN a.triggeredAt >= :since THEN 1 END) as recentAlerts " +
           "FROM SystemAlert a")
    Object[] getAlertStatistics(@Param("since") LocalDateTime since);

    /**
     * Find alerts that need escalation
     */
    @Query("SELECT a FROM SystemAlert a WHERE " +
           "a.status = 'ACTIVE' AND " +
           "a.acknowledgedAt IS NULL AND " +
           "a.severity IN ('CRITICAL', 'HIGH') AND " +
           "a.triggeredAt < :escalationThreshold " +
           "ORDER BY a.triggeredAt ASC")
    List<SystemAlert> findAlertsNeedingEscalation(@Param("escalationThreshold") LocalDateTime escalationThreshold);

    /**
     * Acknowledge alert
     */
    @Modifying
    @Query("UPDATE SystemAlert a SET a.acknowledgedBy = :acknowledgedBy, a.acknowledgedAt = CURRENT_TIMESTAMP WHERE a.alertId = :alertId")
    void acknowledgeAlert(@Param("alertId") Long alertId, @Param("acknowledgedBy") String acknowledgedBy);

    /**
     * Resolve alert
     */
    @Modifying
    @Query("UPDATE SystemAlert a SET a.resolvedBy = :resolvedBy, a.resolvedAt = CURRENT_TIMESTAMP, a.status = 'RESOLVED', a.resolutionNotes = :resolutionNotes WHERE a.alertId = :alertId")
    void resolveAlert(@Param("alertId") Long alertId, @Param("resolvedBy") String resolvedBy, @Param("resolutionNotes") String resolutionNotes);

    /**
     * Auto-resolve alerts based on conditions
     */
    @Modifying
    @Query("UPDATE SystemAlert a SET a.status = 'RESOLVED', a.resolvedAt = CURRENT_TIMESTAMP, a.resolvedBy = 'SYSTEM', a.resolutionNotes = 'Auto-resolved' WHERE a.autoResolve = true AND a.status = 'ACTIVE' AND a.triggeredAt < :autoResolveThreshold")
    void autoResolveOldAlerts(@Param("autoResolveThreshold") LocalDateTime autoResolveThreshold);

    /**
     * Find duplicate alerts (same type and code within time window)
     */
    @Query("SELECT a FROM SystemAlert a WHERE " +
           "a.alertType = :alertType AND " +
           "a.alertCode = :alertCode AND " +
           "a.status = 'ACTIVE' AND " +
           "a.triggeredAt >= :since")
    List<SystemAlert> findDuplicateAlerts(
            @Param("alertType") String alertType,
            @Param("alertCode") String alertCode,
            @Param("since") LocalDateTime since);

    /**
     * Find recent alerts by severity
     */
    @Query("SELECT a FROM SystemAlert a WHERE " +
           "a.severity = :severity AND " +
           "a.triggeredAt >= :since " +
           "ORDER BY a.triggeredAt DESC")
    List<SystemAlert> findRecentAlertsBySeverity(@Param("severity") String severity, @Param("since") LocalDateTime since);

    /**
     * Count alerts by escalation level
     */
    @Query("SELECT a.escalationLevel, COUNT(a) FROM SystemAlert a WHERE a.status = 'ACTIVE' GROUP BY a.escalationLevel")
    List<Object[]> countByEscalationLevel();
}
