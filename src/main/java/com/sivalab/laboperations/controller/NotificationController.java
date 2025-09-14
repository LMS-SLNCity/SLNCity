package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.SystemAlert;
import com.sivalab.laboperations.entity.SystemNotification;
import com.sivalab.laboperations.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing system notifications and alerts
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "System notifications and alerts management")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    // Notification Endpoints

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for a user", description = "Retrieve all notifications for a specific user including system-wide notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<SystemNotification>> getNotificationsForUser(
            @Parameter(description = "User ID") @PathVariable String userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Getting notifications for user: {}, page: {}, size: {}", userId, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<SystemNotification> notifications = notificationService.getNotificationsForUser(userId, pageable);
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications for a user", description = "Retrieve all unread notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SystemNotification>> getUnreadNotificationsForUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        try {
            logger.info("Getting unread notifications for user: {}", userId);
            
            List<SystemNotification> notifications = notificationService.getUnreadNotificationsForUser(userId);
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting unread notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Count unread notifications for a user", description = "Get the count of unread notifications for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> countUnreadNotificationsForUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        try {
            logger.info("Counting unread notifications for user: {}", userId);
            
            long count = notificationService.countUnreadNotificationsForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("unreadCount", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error counting unread notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new notification", description = "Create a new system notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SystemNotification> createNotification(
            @Parameter(description = "Notification type") @RequestParam String type,
            @Parameter(description = "Notification title") @RequestParam String title,
            @Parameter(description = "Notification message") @RequestParam String message,
            @Parameter(description = "Notification severity") @RequestParam(defaultValue = "INFO") String severity) {
        
        try {
            logger.info("Creating notification: type={}, title={}, severity={}", type, title, severity);
            
            SystemNotification notification = notificationService.createNotification(type, title, message, severity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            logger.error("Error creating notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/user")
    @Operation(summary = "Create a user-specific notification", description = "Create a notification for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SystemNotification> createUserNotification(
            @Parameter(description = "Target user ID") @RequestParam String userId,
            @Parameter(description = "Notification type") @RequestParam String type,
            @Parameter(description = "Notification title") @RequestParam String title,
            @Parameter(description = "Notification message") @RequestParam String message,
            @Parameter(description = "Notification severity") @RequestParam(defaultValue = "INFO") String severity) {
        
        try {
            logger.info("Creating user notification: userId={}, type={}, title={}, severity={}", userId, type, title, severity);
            
            SystemNotification notification = notificationService.createUserNotification(userId, type, title, message, severity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            logger.error("Error creating user notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/system-wide")
    @Operation(summary = "Create a system-wide notification", description = "Create a notification visible to all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "System-wide notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SystemNotification> createSystemWideNotification(
            @Parameter(description = "Notification type") @RequestParam String type,
            @Parameter(description = "Notification title") @RequestParam String title,
            @Parameter(description = "Notification message") @RequestParam String message,
            @Parameter(description = "Notification severity") @RequestParam(defaultValue = "INFO") String severity) {
        
        try {
            logger.info("Creating system-wide notification: type={}, title={}, severity={}", type, title, severity);
            
            SystemNotification notification = notificationService.createSystemWideNotification(type, title, message, severity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            logger.error("Error creating system-wide notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long notificationId) {
        
        try {
            logger.info("Marking notification as read: {}", notificationId);
            
            notificationService.markNotificationAsRead(notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notificationId", notificationId);
            response.put("status", "marked_as_read");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", notificationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read for a user", description = "Mark all notifications as read for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All notifications marked as read successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsReadForUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        try {
            logger.info("Marking all notifications as read for user: {}", userId);
            
            notificationService.markAllNotificationsAsReadForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("status", "all_marked_as_read");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Alert Endpoints

    @GetMapping("/alerts")
    @Operation(summary = "Get active alerts", description = "Retrieve all active system alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active alerts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<SystemAlert>> getActiveAlerts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Getting active alerts, page: {}, size: {}", page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<SystemAlert> alerts = notificationService.getActiveAlerts(pageable);
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting active alerts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/critical")
    @Operation(summary = "Get critical alerts", description = "Retrieve all critical system alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Critical alerts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SystemAlert>> getCriticalAlerts() {
        
        try {
            logger.info("Getting critical alerts");
            
            List<SystemAlert> alerts = notificationService.getCriticalAlerts();
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting critical alerts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/unacknowledged")
    @Operation(summary = "Get unacknowledged alerts", description = "Retrieve all unacknowledged system alerts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unacknowledged alerts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SystemAlert>> getUnacknowledgedAlerts() {
        
        try {
            logger.info("Getting unacknowledged alerts");
            
            List<SystemAlert> alerts = notificationService.getUnacknowledgedAlerts();
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting unacknowledged alerts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/alerts")
    @Operation(summary = "Create a new alert", description = "Create a new system alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Alert created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SystemAlert> createAlert(
            @Parameter(description = "Alert type") @RequestParam String alertType,
            @Parameter(description = "Alert code") @RequestParam String alertCode,
            @Parameter(description = "Alert title") @RequestParam String title,
            @Parameter(description = "Alert description") @RequestParam String description,
            @Parameter(description = "Alert severity") @RequestParam(defaultValue = "MEDIUM") String severity) {
        
        try {
            logger.warn("Creating alert: type={}, code={}, title={}, severity={}", alertType, alertCode, title, severity);
            
            SystemAlert alert = notificationService.createAlert(alertType, alertCode, title, description, severity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(alert);
        } catch (Exception e) {
            logger.error("Error creating alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/alerts/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert", description = "Acknowledge a specific system alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(
            @Parameter(description = "Alert ID") @PathVariable Long alertId,
            @Parameter(description = "User acknowledging the alert") @RequestParam String acknowledgedBy) {
        
        try {
            logger.info("Acknowledging alert: {} by {}", alertId, acknowledgedBy);
            
            notificationService.acknowledgeAlert(alertId, acknowledgedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("alertId", alertId);
            response.put("status", "acknowledged");
            response.put("acknowledgedBy", acknowledgedBy);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error acknowledging alert: {}", alertId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve an alert", description = "Resolve a specific system alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resolveAlert(
            @Parameter(description = "Alert ID") @PathVariable Long alertId,
            @Parameter(description = "User resolving the alert") @RequestParam String resolvedBy,
            @Parameter(description = "Resolution notes") @RequestParam(required = false) String resolutionNotes) {
        
        try {
            logger.info("Resolving alert: {} by {} with notes: {}", alertId, resolvedBy, resolutionNotes);
            
            notificationService.resolveAlert(alertId, resolvedBy, resolutionNotes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("alertId", alertId);
            response.put("status", "resolved");
            response.put("resolvedBy", resolvedBy);
            response.put("resolutionNotes", resolutionNotes);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error resolving alert: {}", alertId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistics Endpoints

    @GetMapping("/statistics")
    @Operation(summary = "Get notification statistics", description = "Retrieve comprehensive notification statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {
        
        try {
            logger.info("Getting notification statistics");
            
            Map<String, Object> stats = notificationService.getNotificationStatistics();
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting notification statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/statistics")
    @Operation(summary = "Get alert statistics", description = "Retrieve comprehensive alert statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getAlertStatistics() {
        
        try {
            logger.info("Getting alert statistics");
            
            Map<String, Object> stats = notificationService.getAlertStatistics();
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting alert statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
