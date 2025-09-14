package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing machine ID issues and their resolution
 */
@Entity
@Table(name = "machine_id_issues")
public class MachineIdIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Equipment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private LabEquipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_connection_id")
    private NetworkConnection networkConnection;

    @NotBlank(message = "Issue title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(name = "machine_id_current")
    private String machineIdCurrent;

    @Column(name = "machine_id_expected")
    private String machineIdExpected;

    @Column(name = "mac_address_current")
    private String macAddressCurrent;

    @Column(name = "mac_address_expected")
    private String macAddressExpected;

    @Column(name = "ip_address_current")
    private String ipAddressCurrent;

    @Column(name = "ip_address_expected")
    private String ipAddressExpected;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "first_detected")
    private LocalDateTime firstDetected;

    @Column(name = "last_occurrence")
    private LocalDateTime lastOccurrence;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount = 1;

    @Column(name = "reported_by")
    private String reportedBy;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "estimated_resolution_time_hours")
    private Double estimatedResolutionTimeHours;

    @Column(name = "actual_resolution_time_hours")
    private Double actualResolutionTimeHours;

    @Column(name = "impact_assessment", columnDefinition = "TEXT")
    private String impactAssessment;

    @Column(name = "workaround_applied")
    private String workaroundApplied;

    @Column(name = "root_cause_analysis", columnDefinition = "TEXT")
    private String rootCauseAnalysis;

    @Column(name = "prevention_measures", columnDefinition = "TEXT")
    private String preventionMeasures;

    @Column(name = "escalated")
    private Boolean escalated = false;

    @Column(name = "escalated_to")
    private String escalatedTo;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "priority_level")
    private Integer priorityLevel = 3; // 1=High, 2=Medium, 3=Low

    @Column(name = "auto_detected")
    private Boolean autoDetected = false;

    @Column(name = "requires_physical_access")
    private Boolean requiresPhysicalAccess = false;

    @Column(name = "requires_network_restart")
    private Boolean requiresNetworkRestart = false;

    @Column(name = "requires_equipment_restart")
    private Boolean requiresEquipmentRestart = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diagnostic_data", columnDefinition = "json")
    private JsonNode diagnosticData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolution_steps", columnDefinition = "json")
    private JsonNode resolutionSteps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_logs", columnDefinition = "json")
    private JsonNode relatedLogs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public MachineIdIssue() {}

    public MachineIdIssue(LabEquipment equipment, String title, IssueType issueType) {
        this.equipment = equipment;
        this.title = title;
        this.issueType = issueType;
        this.firstDetected = LocalDateTime.now();
        this.lastOccurrence = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LabEquipment getEquipment() { return equipment; }
    public void setEquipment(LabEquipment equipment) { this.equipment = equipment; }

    public NetworkConnection getNetworkConnection() { return networkConnection; }
    public void setNetworkConnection(NetworkConnection networkConnection) { this.networkConnection = networkConnection; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IssueType getIssueType() { return issueType; }
    public void setIssueType(IssueType issueType) { this.issueType = issueType; }

    public IssueSeverity getSeverity() { return severity; }
    public void setSeverity(IssueSeverity severity) { this.severity = severity; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public String getMachineIdCurrent() { return machineIdCurrent; }
    public void setMachineIdCurrent(String machineIdCurrent) { this.machineIdCurrent = machineIdCurrent; }

    public String getMachineIdExpected() { return machineIdExpected; }
    public void setMachineIdExpected(String machineIdExpected) { this.machineIdExpected = machineIdExpected; }

    public String getMacAddressCurrent() { return macAddressCurrent; }
    public void setMacAddressCurrent(String macAddressCurrent) { this.macAddressCurrent = macAddressCurrent; }

    public String getMacAddressExpected() { return macAddressExpected; }
    public void setMacAddressExpected(String macAddressExpected) { this.macAddressExpected = macAddressExpected; }

    public String getIpAddressCurrent() { return ipAddressCurrent; }
    public void setIpAddressCurrent(String ipAddressCurrent) { this.ipAddressCurrent = ipAddressCurrent; }

    public String getIpAddressExpected() { return ipAddressExpected; }
    public void setIpAddressExpected(String ipAddressExpected) { this.ipAddressExpected = ipAddressExpected; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getFirstDetected() { return firstDetected; }
    public void setFirstDetected(LocalDateTime firstDetected) { this.firstDetected = firstDetected; }

    public LocalDateTime getLastOccurrence() { return lastOccurrence; }
    public void setLastOccurrence(LocalDateTime lastOccurrence) { this.lastOccurrence = lastOccurrence; }

    public Integer getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(Integer occurrenceCount) { this.occurrenceCount = occurrenceCount; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public Double getEstimatedResolutionTimeHours() { return estimatedResolutionTimeHours; }
    public void setEstimatedResolutionTimeHours(Double estimatedResolutionTimeHours) { this.estimatedResolutionTimeHours = estimatedResolutionTimeHours; }

    public Double getActualResolutionTimeHours() { return actualResolutionTimeHours; }
    public void setActualResolutionTimeHours(Double actualResolutionTimeHours) { this.actualResolutionTimeHours = actualResolutionTimeHours; }

    public String getImpactAssessment() { return impactAssessment; }
    public void setImpactAssessment(String impactAssessment) { this.impactAssessment = impactAssessment; }

    public String getWorkaroundApplied() { return workaroundApplied; }
    public void setWorkaroundApplied(String workaroundApplied) { this.workaroundApplied = workaroundApplied; }

    public String getRootCauseAnalysis() { return rootCauseAnalysis; }
    public void setRootCauseAnalysis(String rootCauseAnalysis) { this.rootCauseAnalysis = rootCauseAnalysis; }

    public String getPreventionMeasures() { return preventionMeasures; }
    public void setPreventionMeasures(String preventionMeasures) { this.preventionMeasures = preventionMeasures; }

    public Boolean getEscalated() { return escalated; }
    public void setEscalated(Boolean escalated) { this.escalated = escalated; }

    public String getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }

    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public Boolean getAutoDetected() { return autoDetected; }
    public void setAutoDetected(Boolean autoDetected) { this.autoDetected = autoDetected; }

    public Boolean getRequiresPhysicalAccess() { return requiresPhysicalAccess; }
    public void setRequiresPhysicalAccess(Boolean requiresPhysicalAccess) { this.requiresPhysicalAccess = requiresPhysicalAccess; }

    public Boolean getRequiresNetworkRestart() { return requiresNetworkRestart; }
    public void setRequiresNetworkRestart(Boolean requiresNetworkRestart) { this.requiresNetworkRestart = requiresNetworkRestart; }

    public Boolean getRequiresEquipmentRestart() { return requiresEquipmentRestart; }
    public void setRequiresEquipmentRestart(Boolean requiresEquipmentRestart) { this.requiresEquipmentRestart = requiresEquipmentRestart; }

    public JsonNode getDiagnosticData() { return diagnosticData; }
    public void setDiagnosticData(JsonNode diagnosticData) { this.diagnosticData = diagnosticData; }

    public JsonNode getResolutionSteps() { return resolutionSteps; }
    public void setResolutionSteps(JsonNode resolutionSteps) { this.resolutionSteps = resolutionSteps; }

    public JsonNode getRelatedLogs() { return relatedLogs; }
    public void setRelatedLogs(JsonNode relatedLogs) { this.relatedLogs = relatedLogs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isOpen() {
        return status == IssueStatus.OPEN || status == IssueStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED;
    }

    public boolean isHighPriority() {
        return priorityLevel != null && priorityLevel == 1;
    }

    public boolean isCritical() {
        return severity == IssueSeverity.CRITICAL;
    }

    public void incrementOccurrenceCount() {
        this.occurrenceCount = (this.occurrenceCount == null) ? 1 : this.occurrenceCount + 1;
        this.lastOccurrence = LocalDateTime.now();
    }
}
