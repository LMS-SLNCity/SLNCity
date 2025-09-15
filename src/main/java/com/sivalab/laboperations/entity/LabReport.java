package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_reports")
public class LabReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;
    
    @Column(name = "ulr_number", unique = true, nullable = false, length = 50)
    private String ulrNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType = ReportType.STANDARD;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus = ReportStatus.DRAFT;
    
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    @Column(name = "authorized_by")
    private String authorizedBy;
    
    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_data", columnDefinition = "json")
    private JsonNode reportData;
    
    @Column(name = "template_version", length = 20)
    private String templateVersion;
    
    @Column(name = "nabl_compliant")
    private Boolean nablCompliant = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public LabReport() {}
    
    public LabReport(Visit visit, ReportType reportType) {
        this.visit = visit;
        this.reportType = reportType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }
    
    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }
    
    public String getUlrNumber() {
        return ulrNumber;
    }
    
    public void setUlrNumber(String ulrNumber) {
        this.ulrNumber = ulrNumber;
    }
    
    public Visit getVisit() {
        return visit;
    }
    
    public void setVisit(Visit visit) {
        this.visit = visit;
    }
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    
    public ReportStatus getReportStatus() {
        return reportStatus;
    }
    
    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public String getAuthorizedBy() {
        return authorizedBy;
    }
    
    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }
    
    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }
    
    public void setAuthorizedAt(LocalDateTime authorizedAt) {
        this.authorizedAt = authorizedAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public JsonNode getReportData() {
        return reportData;
    }
    
    public void setReportData(JsonNode reportData) {
        this.reportData = reportData;
    }
    
    public String getTemplateVersion() {
        return templateVersion;
    }
    
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }
    
    public Boolean getNablCompliant() {
        return nablCompliant;
    }
    
    public void setNablCompliant(Boolean nablCompliant) {
        this.nablCompliant = nablCompliant;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark report as generated
     */
    public void markAsGenerated(String templateVersion) {
        this.reportStatus = ReportStatus.GENERATED;
        this.generatedAt = LocalDateTime.now();
        this.templateVersion = templateVersion;
    }
    
    /**
     * Authorize the report
     */
    public void authorize(String authorizedBy) {
        this.reportStatus = ReportStatus.AUTHORIZED;
        this.authorizedBy = authorizedBy;
        this.authorizedAt = LocalDateTime.now();
    }
    
    /**
     * Mark report as sent
     */
    public void markAsSent() {
        this.reportStatus = ReportStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
}
