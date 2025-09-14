package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditTrail entity
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    /**
     * Find audit records by table name
     */
    Page<AuditTrail> findByTableNameOrderByTimestampDesc(String tableName, Pageable pageable);

    /**
     * Find audit records by user ID
     */
    Page<AuditTrail> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit records by action type
     */
    Page<AuditTrail> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * Find audit records by severity
     */
    Page<AuditTrail> findBySeverityOrderByTimestampDesc(String severity, Pageable pageable);

    /**
     * Find audit records by module
     */
    Page<AuditTrail> findByModuleOrderByTimestampDesc(String module, Pageable pageable);

    /**
     * Find audit records within a date range
     */
    Page<AuditTrail> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit records for a specific record ID in a table
     */
    List<AuditTrail> findByTableNameAndRecordIdOrderByTimestampDesc(String tableName, Long recordId);

    /**
     * Find recent audit activities (last 24 hours)
     */
    @Query("SELECT a FROM AuditTrail a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditTrail> findRecentActivities(@Param("since") LocalDateTime since);

    /**
     * Count audit records by action type
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditTrail a GROUP BY a.action")
    List<Object[]> countByAction();

    /**
     * Count audit records by table name
     */
    @Query("SELECT a.tableName, COUNT(a) FROM AuditTrail a GROUP BY a.tableName")
    List<Object[]> countByTableName();

    /**
     * Count audit records by user
     */
    @Query("SELECT a.userId, a.userName, COUNT(a) FROM AuditTrail a WHERE a.userId IS NOT NULL GROUP BY a.userId, a.userName")
    List<Object[]> countByUser();

    /**
     * Find audit records by multiple criteria
     */
    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:tableName IS NULL OR a.tableName = :tableName) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:severity IS NULL OR a.severity = :severity) AND " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditTrail> findByCriteria(
            @Param("tableName") String tableName,
            @Param("userId") String userId,
            @Param("action") String action,
            @Param("severity") String severity,
            @Param("module") String module,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get audit statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(a) as totalRecords, " +
           "COUNT(DISTINCT a.userId) as uniqueUsers, " +
           "COUNT(DISTINCT a.tableName) as tablesAffected, " +
           "COUNT(CASE WHEN a.severity = 'CRITICAL' THEN 1 END) as criticalEvents, " +
           "COUNT(CASE WHEN a.severity = 'HIGH' THEN 1 END) as highSeverityEvents, " +
           "COUNT(CASE WHEN a.timestamp >= :since THEN 1 END) as recentEvents " +
           "FROM AuditTrail a")
    Object[] getAuditStatistics(@Param("since") LocalDateTime since);

    /**
     * Find suspicious activities (multiple failed attempts, unusual patterns)
     */
    @Query("SELECT a FROM AuditTrail a WHERE " +
           "a.severity IN ('HIGH', 'CRITICAL') AND " +
           "a.timestamp >= :since AND " +
           "(a.description LIKE '%failed%' OR a.description LIKE '%error%' OR a.description LIKE '%unauthorized%') " +
           "ORDER BY a.timestamp DESC")
    List<AuditTrail> findSuspiciousActivities(@Param("since") LocalDateTime since);

    /**
     * Delete old audit records (for cleanup)
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);

    /**
     * Count records older than specified date
     */
    long countByTimestampBefore(LocalDateTime cutoffDate);
}
