package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing system alerts for critical events
 */
@Entity
@Table(name = "system_alerts")
@Schema(description = "System alert for critical events")
public class SystemAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    @Schema(description = "Unique identifier for the alert")
    private Long alertId;

    @Column(name = "alert_type", nullable = false, length = 50)
    @Schema(description = "Type of alert")
    private String alertType;

    @Column(name = "alert_code", nullable = false, length = 20)
    @Schema(description = "Alert code for categorization")
    private String alertCode;

    @Column(name = "title", nullable = false)
    @Schema(description = "Title of the alert")
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Detailed description of the alert")
    private String description;

    @Column(name = "severity", nullable = false, length = 20)
    @Schema(description = "Severity level of the alert")
    private String severity = "MEDIUM";

    @Column(name = "status", length = 20)
    @Schema(description = "Current status of the alert")
    private String status = "ACTIVE";

    @Column(name = "source_module", length = 100)
    @Schema(description = "Module that generated the alert")
    private String sourceModule;

    @Column(name = "source_table", length = 100)
    @Schema(description = "Database table related to the alert")
    private String sourceTable;

    @Column(name = "source_record_id")
    @Schema(description = "Record ID related to the alert")
    private Long sourceRecordId;

    @Column(name = "triggered_by")
    @Schema(description = "User or system that triggered the alert")
    private String triggeredBy;

    @Column(name = "triggered_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was triggered")
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged_by")
    @Schema(description = "User who acknowledged the alert")
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was acknowledged")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_by")
    @Schema(description = "User who resolved the alert")
    private String resolvedBy;

    @Column(name = "resolved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was resolved")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    @Schema(description = "Notes about the resolution")
    private String resolutionNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alert_data", columnDefinition = "json")
    @Schema(description = "Additional data related to the alert")
    private JsonNode alertData;

    @Column(name = "auto_resolve")
    @Schema(description = "Whether the alert can be auto-resolved")
    private Boolean autoResolve = false;

    @Column(name = "escalation_level")
    @Schema(description = "Current escalation level")
    private Integer escalationLevel = 1;

    // Constructors
    public SystemAlert() {
        this.triggeredAt = LocalDateTime.now();
    }

    public SystemAlert(String alertType, String alertCode, String title, String description) {
        this();
        this.alertType = alertType;
        this.alertCode = alertCode;
        this.title = title;
        this.description = description;
    }

    public SystemAlert(String alertType, String alertCode, String title, String description, String severity) {
        this(alertType, alertCode, title, description);
        this.severity = severity;
    }

    // Getters and Setters
    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public Long getSourceRecordId() {
        return sourceRecordId;
    }

    public void setSourceRecordId(Long sourceRecordId) {
        this.sourceRecordId = sourceRecordId;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public JsonNode getAlertData() {
        return alertData;
    }

    public void setAlertData(JsonNode alertData) {
        this.alertData = alertData;
    }

    public Boolean getAutoResolve() {
        return autoResolve;
    }

    public void setAutoResolve(Boolean autoResolve) {
        this.autoResolve = autoResolve;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    // Utility methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isResolved() {
        return "RESOLVED".equals(status);
    }

    public boolean isAcknowledged() {
        return acknowledgedAt != null;
    }

    public void acknowledge(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve(String resolvedBy, String resolutionNotes) {
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
        this.status = "RESOLVED";
    }

    @Override
    public String toString() {
        return "SystemAlert{" +
                "alertId=" + alertId +
                ", alertType='" + alertType + '\'' +
                ", alertCode='" + alertCode + '\'' +
                ", title='" + title + '\'' +
                ", severity='" + severity + '\'' +
                ", status='" + status + '\'' +
                ", sourceModule='" + sourceModule + '\'' +
                ", triggeredBy='" + triggeredBy + '\'' +
                ", triggeredAt=" + triggeredAt +
                ", escalationLevel=" + escalationLevel +
                '}';
    }
}
