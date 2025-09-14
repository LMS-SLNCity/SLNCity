package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.SystemNotification;
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
 * Repository interface for SystemNotification entity
 */
@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    /**
     * Find notifications for a specific user (including system-wide notifications)
     */
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "(n.targetUserId = :userId OR n.isSystemWide = true) AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY n.createdAt DESC")
    Page<SystemNotification> findNotificationsForUser(@Param("userId") String userId, Pageable pageable);

    /**
     * Find unread notifications for a specific user
     */
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "(n.targetUserId = :userId OR n.isSystemWide = true) AND " +
           "n.isRead = false AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY n.createdAt DESC")
    List<SystemNotification> findUnreadNotificationsForUser(@Param("userId") String userId);

    /**
     * Find notifications by type
     */
    Page<SystemNotification> findByNotificationTypeOrderByCreatedAtDesc(String notificationType, Pageable pageable);

    /**
     * Find notifications by severity
     */
    Page<SystemNotification> findBySeverityOrderByCreatedAtDesc(String severity, Pageable pageable);

    /**
     * Find system-wide notifications
     */
    Page<SystemNotification> findByIsSystemWideTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find notifications for a specific role
     */
    Page<SystemNotification> findByTargetRoleOrderByCreatedAtDesc(String targetRole, Pageable pageable);

    /**
     * Find notifications within a date range
     */
    Page<SystemNotification> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    @Query("SELECT COUNT(n) FROM SystemNotification n WHERE " +
           "(n.targetUserId = :userId OR n.isSystemWide = true) AND " +
           "n.isRead = false AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP)")
    long countUnreadNotificationsForUser(@Param("userId") String userId);

    /**
     * Count notifications by severity
     */
    @Query("SELECT n.severity, COUNT(n) FROM SystemNotification n GROUP BY n.severity")
    List<Object[]> countBySeverity();

    /**
     * Count notifications by type
     */
    @Query("SELECT n.notificationType, COUNT(n) FROM SystemNotification n GROUP BY n.notificationType")
    List<Object[]> countByType();

    /**
     * Mark notification as read
     */
    @Modifying
    @Query("UPDATE SystemNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE SystemNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE " +
           "(n.targetUserId = :userId OR n.isSystemWide = true) AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") String userId);

    /**
     * Delete expired notifications
     */
    @Modifying
    @Query("DELETE FROM SystemNotification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredNotifications();

    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM SystemNotification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < CURRENT_TIMESTAMP")
    List<SystemNotification> findExpiredNotifications();

    /**
     * Find notifications by multiple criteria
     */
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "(:notificationType IS NULL OR n.notificationType = :notificationType) AND " +
           "(:severity IS NULL OR n.severity = :severity) AND " +
           "(:targetUserId IS NULL OR n.targetUserId = :targetUserId) AND " +
           "(:targetRole IS NULL OR n.targetRole = :targetRole) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:isSystemWide IS NULL OR n.isSystemWide = :isSystemWide) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "ORDER BY n.createdAt DESC")
    Page<SystemNotification> findByCriteria(
            @Param("notificationType") String notificationType,
            @Param("severity") String severity,
            @Param("targetUserId") String targetUserId,
            @Param("targetRole") String targetRole,
            @Param("isRead") Boolean isRead,
            @Param("isSystemWide") Boolean isSystemWide,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get notification statistics
     */
    @Query("SELECT " +
           "COUNT(n) as totalNotifications, " +
           "COUNT(CASE WHEN n.isRead = false THEN 1 END) as unreadNotifications, " +
           "COUNT(CASE WHEN n.isSystemWide = true THEN 1 END) as systemWideNotifications, " +
           "COUNT(CASE WHEN n.severity = 'CRITICAL' THEN 1 END) as criticalNotifications, " +
           "COUNT(CASE WHEN n.severity = 'HIGH' THEN 1 END) as highSeverityNotifications, " +
           "COUNT(CASE WHEN n.createdAt >= :since THEN 1 END) as recentNotifications " +
           "FROM SystemNotification n")
    Object[] getNotificationStatistics(@Param("since") LocalDateTime since);

    /**
     * Find recent high-priority notifications
     */
    @Query("SELECT n FROM SystemNotification n WHERE " +
           "n.severity IN ('CRITICAL', 'HIGH') AND " +
           "n.createdAt >= :since AND " +
           "(n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP) " +
           "ORDER BY n.createdAt DESC")
    List<SystemNotification> findRecentHighPriorityNotifications(@Param("since") LocalDateTime since);
}
