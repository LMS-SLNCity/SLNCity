package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalab.laboperations.entity.SystemAlert;
import com.sivalab.laboperations.entity.SystemNotification;
import com.sivalab.laboperations.repository.SystemAlertRepository;
import com.sivalab.laboperations.repository.SystemNotificationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing system notifications and alerts
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SystemNotificationRepository notificationRepository;

    @Autowired
    private SystemAlertRepository alertRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Notification Management

    /**
     * Create a new notification
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createNotificationFallback")
    @Retry(name = "database")
    public SystemNotification createNotification(String type, String title, String message, String severity) {
        logger.info("Creating notification: type={}, title={}, severity={}", type, title, severity);
        
        SystemNotification notification = new SystemNotification(type, title, message, severity);
        return notificationRepository.save(notification);
    }

    /**
     * Create a user-specific notification
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createUserNotificationFallback")
    @Retry(name = "database")
    public SystemNotification createUserNotification(String userId, String type, String title, String message, String severity) {
        logger.info("Creating user notification: userId={}, type={}, title={}, severity={}", userId, type, title, severity);
        
        SystemNotification notification = new SystemNotification(type, title, message, severity);
        notification.setTargetUserId(userId);
        return notificationRepository.save(notification);
    }

    /**
     * Create a role-based notification
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createRoleNotificationFallback")
    @Retry(name = "database")
    public SystemNotification createRoleNotification(String role, String type, String title, String message, String severity) {
        logger.info("Creating role notification: role={}, type={}, title={}, severity={}", role, type, title, severity);
        
        SystemNotification notification = new SystemNotification(type, title, message, severity);
        notification.setTargetRole(role);
        return notificationRepository.save(notification);
    }

    /**
     * Create a system-wide notification
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createSystemNotificationFallback")
    @Retry(name = "database")
    public SystemNotification createSystemWideNotification(String type, String title, String message, String severity) {
        logger.info("Creating system-wide notification: type={}, title={}, severity={}", type, title, severity);
        
        SystemNotification notification = new SystemNotification(type, title, message, severity);
        notification.setIsSystemWide(true);
        return notificationRepository.save(notification);
    }

    /**
     * Get notifications for a user
     */
    @CircuitBreaker(name = "database", fallbackMethod = "getNotificationsForUserFallback")
    @Retry(name = "database")
    public Page<SystemNotification> getNotificationsForUser(String userId, Pageable pageable) {
        logger.debug("Getting notifications for user: {}", userId);
        return notificationRepository.findNotificationsForUser(userId, pageable);
    }

    /**
     * Get unread notifications for a user
     */
    @CircuitBreaker(name = "database", fallbackMethod = "getUnreadNotificationsFallback")
    @Retry(name = "database")
    public List<SystemNotification> getUnreadNotificationsForUser(String userId) {
        logger.debug("Getting unread notifications for user: {}", userId);
        return notificationRepository.findUnreadNotificationsForUser(userId);
    }

    /**
     * Mark notification as read
     */
    @CircuitBreaker(name = "database", fallbackMethod = "markAsReadFallback")
    @Retry(name = "database")
    public void markNotificationAsRead(Long notificationId) {
        logger.info("Marking notification as read: {}", notificationId);
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read for a user
     */
    @CircuitBreaker(name = "database", fallbackMethod = "markAllAsReadFallback")
    @Retry(name = "database")
    public void markAllNotificationsAsReadForUser(String userId) {
        logger.info("Marking all notifications as read for user: {}", userId);
        notificationRepository.markAllAsReadForUser(userId);
    }

    /**
     * Count unread notifications for a user
     */
    @CircuitBreaker(name = "database", fallbackMethod = "countUnreadFallback")
    @Retry(name = "database")
    public long countUnreadNotificationsForUser(String userId) {
        logger.debug("Counting unread notifications for user: {}", userId);
        return notificationRepository.countUnreadNotificationsForUser(userId);
    }

    // Alert Management

    /**
     * Create a new alert
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createAlertFallback")
    @Retry(name = "database")
    public SystemAlert createAlert(String alertType, String alertCode, String title, String description, String severity) {
        logger.warn("Creating alert: type={}, code={}, title={}, severity={}", alertType, alertCode, title, severity);
        
        // Check for duplicate alerts within the last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<SystemAlert> duplicates = alertRepository.findDuplicateAlerts(alertType, alertCode, oneHourAgo);
        
        if (!duplicates.isEmpty()) {
            logger.info("Duplicate alert found, updating escalation level instead of creating new alert");
            SystemAlert existingAlert = duplicates.get(0);
            existingAlert.setEscalationLevel(existingAlert.getEscalationLevel() + 1);
            return alertRepository.save(existingAlert);
        }
        
        SystemAlert alert = new SystemAlert(alertType, alertCode, title, description, severity);
        SystemAlert savedAlert = alertRepository.save(alert);
        
        // Create corresponding notification for critical and high severity alerts
        if ("CRITICAL".equals(severity) || "HIGH".equals(severity)) {
            createSystemWideNotification("ALERT", "System Alert: " + title, description, severity);
        }
        
        return savedAlert;
    }

    /**
     * Create alert with source information
     */
    @CircuitBreaker(name = "database", fallbackMethod = "createAlertWithSourceFallback")
    @Retry(name = "database")
    public SystemAlert createAlert(String alertType, String alertCode, String title, String description, 
                                 String severity, String sourceModule, String sourceTable, Long sourceRecordId) {
        logger.warn("Creating alert with source: type={}, code={}, title={}, severity={}, module={}", 
                   alertType, alertCode, title, severity, sourceModule);
        
        SystemAlert alert = createAlert(alertType, alertCode, title, description, severity);
        alert.setSourceModule(sourceModule);
        alert.setSourceTable(sourceTable);
        alert.setSourceRecordId(sourceRecordId);
        
        return alertRepository.save(alert);
    }

    /**
     * Acknowledge an alert
     */
    @CircuitBreaker(name = "database", fallbackMethod = "acknowledgeAlertFallback")
    @Retry(name = "database")
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        logger.info("Acknowledging alert: {} by {}", alertId, acknowledgedBy);
        alertRepository.acknowledgeAlert(alertId, acknowledgedBy);
    }

    /**
     * Resolve an alert
     */
    @CircuitBreaker(name = "database", fallbackMethod = "resolveAlertFallback")
    @Retry(name = "database")
    public void resolveAlert(Long alertId, String resolvedBy, String resolutionNotes) {
        logger.info("Resolving alert: {} by {} with notes: {}", alertId, resolvedBy, resolutionNotes);
        alertRepository.resolveAlert(alertId, resolvedBy, resolutionNotes);
    }

    /**
     * Get active alerts
     */
    @CircuitBreaker(name = "database", fallbackMethod = "getActiveAlertsFallback")
    @Retry(name = "database")
    public Page<SystemAlert> getActiveAlerts(Pageable pageable) {
        logger.debug("Getting active alerts");
        return alertRepository.findByStatusOrderByTriggeredAtDesc("ACTIVE", pageable);
    }

    /**
     * Get critical alerts
     */
    @CircuitBreaker(name = "database", fallbackMethod = "getCriticalAlertsFallback")
    @Retry(name = "database")
    public List<SystemAlert> getCriticalAlerts() {
        logger.debug("Getting critical alerts");
        return alertRepository.findCriticalAlerts();
    }

    /**
     * Get unacknowledged alerts
     */
    @CircuitBreaker(name = "database", fallbackMethod = "getUnacknowledgedAlertsFallback")
    @Retry(name = "database")
    public List<SystemAlert> getUnacknowledgedAlerts() {
        logger.debug("Getting unacknowledged alerts");
        return alertRepository.findUnacknowledgedAlerts();
    }

    // Statistics and Analytics

    /**
     * Get notification statistics
     */
    @Cacheable(value = "notificationStatistics", unless = "#result.containsKey('error')")
    @CircuitBreaker(name = "database", fallbackMethod = "getNotificationStatisticsFallback")
    @Retry(name = "database")
    public Map<String, Object> getNotificationStatistics() {
        logger.debug("Getting notification statistics");
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Object[] stats = notificationRepository.getNotificationStatistics(last24Hours);
        
        Map<String, Object> result = new HashMap<>();
        if (stats != null && stats.length >= 6) {
            result.put("totalNotifications", stats[0]);
            result.put("unreadNotifications", stats[1]);
            result.put("systemWideNotifications", stats[2]);
            result.put("criticalNotifications", stats[3]);
            result.put("highSeverityNotifications", stats[4]);
            result.put("recentNotifications", stats[5]);
        }
        
        return result;
    }

    /**
     * Get alert statistics
     */
    @Cacheable(value = "alertStatistics", unless = "#result.containsKey('error')")
    @CircuitBreaker(name = "database", fallbackMethod = "getAlertStatisticsFallback")
    @Retry(name = "database")
    public Map<String, Object> getAlertStatistics() {
        logger.debug("Getting alert statistics");
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Object[] stats = alertRepository.getAlertStatistics(last24Hours);
        
        Map<String, Object> result = new HashMap<>();
        if (stats != null && stats.length >= 7) {
            result.put("totalAlerts", stats[0]);
            result.put("activeAlerts", stats[1]);
            result.put("resolvedAlerts", stats[2]);
            result.put("unacknowledgedAlerts", stats[3]);
            result.put("criticalAlerts", stats[4]);
            result.put("highSeverityAlerts", stats[5]);
            result.put("recentAlerts", stats[6]);
        }
        
        return result;
    }

    // Async Operations

    /**
     * Async notification creation
     */
    @Async
    @CircuitBreaker(name = "database")
    public CompletableFuture<SystemNotification> createNotificationAsync(String type, String title, String message, String severity) {
        logger.info("Creating notification asynchronously: type={}, title={}, severity={}", type, title, severity);
        SystemNotification notification = createNotification(type, title, message, severity);
        return CompletableFuture.completedFuture(notification);
    }

    /**
     * Async alert creation
     */
    @Async
    @CircuitBreaker(name = "database")
    public CompletableFuture<SystemAlert> createAlertAsync(String alertType, String alertCode, String title, String description, String severity) {
        logger.warn("Creating alert asynchronously: type={}, code={}, title={}, severity={}", alertType, alertCode, title, severity);
        SystemAlert alert = createAlert(alertType, alertCode, title, description, severity);
        return CompletableFuture.completedFuture(alert);
    }

    // Cleanup Operations

    /**
     * Clean up expired notifications
     */
    @CircuitBreaker(name = "database", fallbackMethod = "cleanupExpiredNotificationsFallback")
    @Retry(name = "database")
    public void cleanupExpiredNotifications() {
        logger.info("Cleaning up expired notifications");
        notificationRepository.deleteExpiredNotifications();
    }

    /**
     * Auto-resolve old alerts
     */
    @CircuitBreaker(name = "database", fallbackMethod = "autoResolveOldAlertsFallback")
    @Retry(name = "database")
    public void autoResolveOldAlerts() {
        logger.info("Auto-resolving old alerts");
        LocalDateTime threshold = LocalDateTime.now().minusDays(7); // Auto-resolve alerts older than 7 days
        alertRepository.autoResolveOldAlerts(threshold);
    }

    // Fallback Methods

    private SystemNotification createNotificationFallback(String type, String title, String message, String severity, Exception ex) {
        logger.error("Failed to create notification, using fallback", ex);
        return new SystemNotification(type, "Fallback: " + title, "Service temporarily unavailable: " + message, "INFO");
    }

    private SystemNotification createUserNotificationFallback(String userId, String type, String title, String message, String severity, Exception ex) {
        logger.error("Failed to create user notification, using fallback", ex);
        return createNotificationFallback(type, title, message, severity, ex);
    }

    private SystemNotification createRoleNotificationFallback(String role, String type, String title, String message, String severity, Exception ex) {
        logger.error("Failed to create role notification, using fallback", ex);
        return createNotificationFallback(type, title, message, severity, ex);
    }

    private SystemNotification createSystemNotificationFallback(String type, String title, String message, String severity, Exception ex) {
        logger.error("Failed to create system notification, using fallback", ex);
        return createNotificationFallback(type, title, message, severity, ex);
    }

    private Page<SystemNotification> getNotificationsForUserFallback(String userId, Pageable pageable, Exception ex) {
        logger.error("Failed to get notifications for user, using fallback", ex);
        return Page.empty();
    }

    private List<SystemNotification> getUnreadNotificationsFallback(String userId, Exception ex) {
        logger.error("Failed to get unread notifications, using fallback", ex);
        return List.of();
    }

    private void markAsReadFallback(Long notificationId, Exception ex) {
        logger.error("Failed to mark notification as read", ex);
    }

    private void markAllAsReadFallback(String userId, Exception ex) {
        logger.error("Failed to mark all notifications as read", ex);
    }

    private long countUnreadFallback(String userId, Exception ex) {
        logger.error("Failed to count unread notifications", ex);
        return 0;
    }

    private SystemAlert createAlertFallback(String alertType, String alertCode, String title, String description, String severity, Exception ex) {
        logger.error("Failed to create alert, using fallback", ex);
        return new SystemAlert(alertType, alertCode, "Fallback: " + title, "Service temporarily unavailable: " + description, "INFO");
    }

    private SystemAlert createAlertWithSourceFallback(String alertType, String alertCode, String title, String description, 
                                                    String severity, String sourceModule, String sourceTable, Long sourceRecordId, Exception ex) {
        logger.error("Failed to create alert with source, using fallback", ex);
        return createAlertFallback(alertType, alertCode, title, description, severity, ex);
    }

    private void acknowledgeAlertFallback(Long alertId, String acknowledgedBy, Exception ex) {
        logger.error("Failed to acknowledge alert", ex);
    }

    private void resolveAlertFallback(Long alertId, String resolvedBy, String resolutionNotes, Exception ex) {
        logger.error("Failed to resolve alert", ex);
    }

    private Page<SystemAlert> getActiveAlertsFallback(Pageable pageable, Exception ex) {
        logger.error("Failed to get active alerts, using fallback", ex);
        return Page.empty();
    }

    private List<SystemAlert> getCriticalAlertsFallback(Exception ex) {
        logger.error("Failed to get critical alerts, using fallback", ex);
        return List.of();
    }

    private List<SystemAlert> getUnacknowledgedAlertsFallback(Exception ex) {
        logger.error("Failed to get unacknowledged alerts, using fallback", ex);
        return List.of();
    }

    private Map<String, Object> getNotificationStatisticsFallback(Exception ex) {
        logger.error("Failed to get notification statistics, using fallback", ex);
        return Map.of("error", "Statistics temporarily unavailable");
    }

    private Map<String, Object> getAlertStatisticsFallback(Exception ex) {
        logger.error("Failed to get alert statistics, using fallback", ex);
        return Map.of("error", "Statistics temporarily unavailable");
    }

    private void cleanupExpiredNotificationsFallback(Exception ex) {
        logger.error("Failed to cleanup expired notifications", ex);
    }

    private void autoResolveOldAlertsFallback(Exception ex) {
        logger.error("Failed to auto-resolve old alerts", ex);
    }
}
