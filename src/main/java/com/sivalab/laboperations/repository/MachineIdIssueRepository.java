package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.MachineIdIssue;
import com.sivalab.laboperations.entity.IssueType;
import com.sivalab.laboperations.entity.IssueSeverity;
import com.sivalab.laboperations.entity.IssueStatus;
import com.sivalab.laboperations.entity.LabEquipment;
import com.sivalab.laboperations.entity.NetworkConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for MachineIdIssue entity
 */
@Repository
public interface MachineIdIssueRepository extends JpaRepository<MachineIdIssue, Long> {

    /**
     * Find issues by equipment
     */
    List<MachineIdIssue> findByEquipment(LabEquipment equipment);

    /**
     * Find issues by equipment ID
     */
    List<MachineIdIssue> findByEquipmentId(Long equipmentId);

    /**
     * Find issues by network connection
     */
    List<MachineIdIssue> findByNetworkConnection(NetworkConnection networkConnection);

    /**
     * Find issues by issue type
     */
    List<MachineIdIssue> findByIssueType(IssueType issueType);

    /**
     * Find issues by severity
     */
    List<MachineIdIssue> findBySeverity(IssueSeverity severity);

    /**
     * Find issues by status
     */
    List<MachineIdIssue> findByStatus(IssueStatus status);

    /**
     * Find open issues
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE mi.status IN ('OPEN', 'IN_PROGRESS')")
    List<MachineIdIssue> findOpenIssues();

    /**
     * Find resolved issues
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE mi.status IN ('RESOLVED', 'CLOSED')")
    List<MachineIdIssue> findResolvedIssues();

    /**
     * Find critical issues
     */
    List<MachineIdIssue> findBySeverityOrderByCreatedAtDesc(IssueSeverity severity);

    /**
     * Find high priority issues
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE mi.priorityLevel = 1 ORDER BY mi.createdAt DESC")
    List<MachineIdIssue> findHighPriorityIssues();

    /**
     * Find escalated issues
     */
    List<MachineIdIssue> findByEscalatedTrue();

    /**
     * Find auto-detected issues
     */
    List<MachineIdIssue> findByAutoDetectedTrue();

    /**
     * Find issues assigned to a specific person
     */
    List<MachineIdIssue> findByAssignedTo(String assignedTo);

    /**
     * Find issues reported by a specific person
     */
    List<MachineIdIssue> findByReportedBy(String reportedBy);

    /**
     * Find issues resolved by a specific person
     */
    List<MachineIdIssue> findByResolvedBy(String resolvedBy);

    /**
     * Find issues by machine ID
     */
    List<MachineIdIssue> findByMachineIdCurrent(String machineId);

    /**
     * Find issues by MAC address
     */
    List<MachineIdIssue> findByMacAddressCurrent(String macAddress);

    /**
     * Find issues by IP address
     */
    List<MachineIdIssue> findByIpAddressCurrent(String ipAddress);

    /**
     * Find issues by error code
     */
    List<MachineIdIssue> findByErrorCode(String errorCode);

    /**
     * Find issues created after a specific date
     */
    List<MachineIdIssue> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find issues created before a specific date
     */
    List<MachineIdIssue> findByCreatedAtBefore(LocalDateTime dateTime);

    /**
     * Find issues created between dates
     */
    List<MachineIdIssue> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find issues first detected after a specific date
     */
    List<MachineIdIssue> findByFirstDetectedAfter(LocalDateTime dateTime);

    /**
     * Find issues with last occurrence after a specific date
     */
    List<MachineIdIssue> findByLastOccurrenceAfter(LocalDateTime dateTime);

    /**
     * Find issues resolved after a specific date
     */
    List<MachineIdIssue> findByResolvedAtAfter(LocalDateTime dateTime);

    /**
     * Find issues with occurrence count above threshold
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE mi.occurrenceCount > :threshold")
    List<MachineIdIssue> findIssuesWithHighOccurrenceCount(@Param("threshold") Integer threshold);

    /**
     * Find issues requiring physical access
     */
    List<MachineIdIssue> findByRequiresPhysicalAccessTrue();

    /**
     * Find issues requiring network restart
     */
    List<MachineIdIssue> findByRequiresNetworkRestartTrue();

    /**
     * Find issues requiring equipment restart
     */
    List<MachineIdIssue> findByRequiresEquipmentRestartTrue();

    /**
     * Find overdue issues (estimated resolution time exceeded)
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE " +
           "mi.status IN ('OPEN', 'IN_PROGRESS') AND " +
           "mi.estimatedResolutionTimeHours IS NOT NULL AND " +
           "mi.createdAt < :overdueThreshold")
    List<MachineIdIssue> findOverdueIssues(@Param("overdueThreshold") LocalDateTime overdueThreshold);

    /**
     * Get issue statistics by type
     */
    @Query("SELECT mi.issueType, COUNT(mi) FROM MachineIdIssue mi GROUP BY mi.issueType")
    List<Object[]> getIssueTypeStatistics();

    /**
     * Get issue statistics by severity
     */
    @Query("SELECT mi.severity, COUNT(mi) FROM MachineIdIssue mi GROUP BY mi.severity")
    List<Object[]> getIssueSeverityStatistics();

    /**
     * Get issue statistics by status
     */
    @Query("SELECT mi.status, COUNT(mi) FROM MachineIdIssue mi GROUP BY mi.status")
    List<Object[]> getIssueStatusStatistics();

    /**
     * Get issue statistics by equipment type
     */
    @Query("SELECT e.equipmentType, COUNT(mi) FROM MachineIdIssue mi " +
           "JOIN mi.equipment e GROUP BY e.equipmentType")
    List<Object[]> getIssueStatisticsByEquipmentType();

    /**
     * Get average resolution time by issue type
     */
    @Query("SELECT mi.issueType, AVG(mi.actualResolutionTimeHours) FROM MachineIdIssue mi " +
           "WHERE mi.actualResolutionTimeHours IS NOT NULL GROUP BY mi.issueType")
    List<Object[]> getAverageResolutionTimeByType();

    /**
     * Get issue count by month
     */
    @Query("SELECT YEAR(mi.createdAt), MONTH(mi.createdAt), COUNT(mi) FROM MachineIdIssue mi " +
           "GROUP BY YEAR(mi.createdAt), MONTH(mi.createdAt) ORDER BY YEAR(mi.createdAt), MONTH(mi.createdAt)")
    List<Object[]> getIssueCountByMonth();

    /**
     * Get top error codes
     */
    @Query("SELECT mi.errorCode, COUNT(mi) FROM MachineIdIssue mi " +
           "WHERE mi.errorCode IS NOT NULL " +
           "GROUP BY mi.errorCode ORDER BY COUNT(mi) DESC")
    List<Object[]> getTopErrorCodes();

    /**
     * Get issues by assignee workload
     */
    @Query("SELECT mi.assignedTo, COUNT(mi) FROM MachineIdIssue mi " +
           "WHERE mi.assignedTo IS NOT NULL AND mi.status IN ('OPEN', 'IN_PROGRESS') " +
           "GROUP BY mi.assignedTo ORDER BY COUNT(mi) DESC")
    List<Object[]> getIssueWorkloadByAssignee();

    /**
     * Count open issues by equipment
     */
    @Query("SELECT mi.equipment, COUNT(mi) FROM MachineIdIssue mi " +
           "WHERE mi.status IN ('OPEN', 'IN_PROGRESS') " +
           "GROUP BY mi.equipment ORDER BY COUNT(mi) DESC")
    List<Object[]> getOpenIssueCountByEquipment();

    /**
     * Count issues by priority level
     */
    @Query("SELECT mi.priorityLevel, COUNT(mi) FROM MachineIdIssue mi GROUP BY mi.priorityLevel")
    List<Object[]> getIssueCountByPriorityLevel();

    /**
     * Find recent issues (last 24 hours)
     */
    @Query("SELECT mi FROM MachineIdIssue mi WHERE mi.createdAt > :since ORDER BY mi.createdAt DESC")
    List<MachineIdIssue> findRecentIssues(@Param("since") LocalDateTime since);

    /**
     * Count total open issues
     */
    @Query("SELECT COUNT(mi) FROM MachineIdIssue mi WHERE mi.status IN ('OPEN', 'IN_PROGRESS')")
    Long countOpenIssues();

    /**
     * Count total resolved issues
     */
    @Query("SELECT COUNT(mi) FROM MachineIdIssue mi WHERE mi.status IN ('RESOLVED', 'CLOSED')")
    Long countResolvedIssues();

    /**
     * Count critical issues
     */
    Long countBySeverity(IssueSeverity severity);

    /**
     * Count escalated issues
     */
    Long countByEscalatedTrue();
}
