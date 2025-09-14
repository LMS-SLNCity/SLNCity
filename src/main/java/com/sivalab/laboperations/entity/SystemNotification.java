package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing system notifications for users
 */
@Entity
@Table(name = "system_notifications")
@Schema(description = "System notification for users")
public class SystemNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    @Schema(description = "Unique identifier for the notification")
    private Long notificationId;

    @Column(name = "notification_type", nullable = false, length = 50)
    @Schema(description = "Type of notification")
    private String notificationType;

    @Column(name = "title", nullable = false)
    @Schema(description = "Title of the notification")
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Message content of the notification")
    private String message;

    @Column(name = "severity", length = 20)
    @Schema(description = "Severity level of the notification")
    private String severity = "INFO";

    @Column(name = "target_user_id")
    @Schema(description = "Target user ID for the notification")
    private String targetUserId;

    @Column(name = "target_role", length = 100)
    @Schema(description = "Target user role for the notification")
    private String targetRole;

    @Column(name = "is_read")
    @Schema(description = "Whether the notification has been read")
    private Boolean isRead = false;

    @Column(name = "is_system_wide")
    @Schema(description = "Whether this is a system-wide notification")
    private Boolean isSystemWide = false;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the notification was created")
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the notification was read")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the notification expires")
    private LocalDateTime expiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    @Schema(description = "Additional metadata for the notification")
    private JsonNode metadata;

    // Constructors
    public SystemNotification() {
        this.createdAt = LocalDateTime.now();
    }

    public SystemNotification(String notificationType, String title, String message) {
        this();
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
    }

    public SystemNotification(String notificationType, String title, String message, String severity) {
        this(notificationType, title, message);
        this.severity = severity;
    }

    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public Boolean getIsSystemWide() {
        return isSystemWide;
    }

    public void setIsSystemWide(Boolean isSystemWide) {
        this.isSystemWide = isSystemWide;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SystemNotification{" +
                "notificationId=" + notificationId +
                ", notificationType='" + notificationType + '\'' +
                ", title='" + title + '\'' +
                ", severity='" + severity + '\'' +
                ", targetUserId='" + targetUserId + '\'' +
                ", targetRole='" + targetRole + '\'' +
                ", isRead=" + isRead +
                ", isSystemWide=" + isSystemWide +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
