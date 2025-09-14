package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing audit trail records for tracking all database changes
 */
@Entity
@Table(name = "audit_trail")
@Schema(description = "Audit trail record for tracking database changes")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    @Schema(description = "Unique identifier for the audit record")
    private Long auditId;

    @Column(name = "table_name", nullable = false, length = 100)
    @Schema(description = "Name of the table that was modified")
    private String tableName;

    @Column(name = "record_id")
    @Schema(description = "ID of the record that was modified")
    private Long recordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    @Schema(description = "Type of action performed")
    private AuditAction action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "json")
    @Schema(description = "Previous values before the change")
    private JsonNode oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "json")
    @Schema(description = "New values after the change")
    private JsonNode newValues;

    @ElementCollection
    @CollectionTable(name = "audit_changed_fields", joinColumns = @JoinColumn(name = "audit_id"))
    @Column(name = "field_name")
    @Schema(description = "List of fields that were changed")
    private List<String> changedFields;

    @Column(name = "user_id")
    @Schema(description = "ID of the user who performed the action")
    private String userId;

    @Column(name = "user_name")
    @Schema(description = "Name of the user who performed the action")
    private String userName;

    @Column(name = "user_role", length = 100)
    @Schema(description = "Role of the user who performed the action")
    private String userRole;

    @Column(name = "ip_address")
    @Schema(description = "IP address from which the action was performed")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    @Schema(description = "User agent string of the client")
    private String userAgent;

    @Column(name = "session_id")
    @Schema(description = "Session ID of the user")
    private String sessionId;

    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp when the action was performed")
    private LocalDateTime timestamp;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Description of the action performed")
    private String description;

    @Column(name = "severity", length = 20)
    @Schema(description = "Severity level of the audit event")
    private String severity = "INFO";

    @Column(name = "module", length = 100)
    @Schema(description = "Module or component that triggered the audit")
    private String module;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_data", columnDefinition = "json")
    @Schema(description = "Additional data related to the audit event")
    private JsonNode additionalData;

    // Constructors
    public AuditTrail() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditTrail(String tableName, Long recordId, AuditAction action) {
        this();
        this.tableName = tableName;
        this.recordId = recordId;
        this.action = action;
    }

    // Getters and Setters
    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public JsonNode getOldValues() {
        return oldValues;
    }

    public void setOldValues(JsonNode oldValues) {
        this.oldValues = oldValues;
    }

    public JsonNode getNewValues() {
        return newValues;
    }

    public void setNewValues(JsonNode newValues) {
        this.newValues = newValues;
    }

    public List<String> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(List<String> changedFields) {
        this.changedFields = changedFields;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public JsonNode getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(JsonNode additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "AuditTrail{" +
                "auditId=" + auditId +
                ", tableName='" + tableName + '\'' +
                ", recordId=" + recordId +
                ", action=" + action +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", severity='" + severity + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}

/**
 * Enum representing different types of audit actions
 */
enum AuditAction {
    INSERT,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    ACCESS,
    EXPORT,
    PRINT
}
