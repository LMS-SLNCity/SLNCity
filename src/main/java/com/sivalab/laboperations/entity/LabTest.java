package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
public class LabTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_template_id", nullable = false)
    private TestTemplate testTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TestStatus status = TestStatus.PENDING;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", columnDefinition = "json")
    private JsonNode results;
    
    @Column(name = "approved")
    private Boolean approved = false;
    
    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "results_entered_at")
    private LocalDateTime resultsEnteredAt;

    // Sample collection tracking
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sample_id")
    private Sample sample;

    // Machine tracking for internal audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private LabEquipment equipment;

    @Column(name = "machine_used")
    private String machineUsed; // For internal tracking, not printed on reports

    @Column(name = "test_started_at")
    private LocalDateTime testStartedAt;

    @Column(name = "test_completed_at")
    private LocalDateTime testCompletedAt;

    // Constructors
    public LabTest() {}
    
    public LabTest(Visit visit, TestTemplate testTemplate, BigDecimal price) {
        this.visit = visit;
        this.testTemplate = testTemplate;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getTestId() {
        return testId;
    }
    
    public void setTestId(Long testId) {
        this.testId = testId;
    }
    
    public Visit getVisit() {
        return visit;
    }
    
    public void setVisit(Visit visit) {
        this.visit = visit;
    }
    
    public TestTemplate getTestTemplate() {
        return testTemplate;
    }
    
    public void setTestTemplate(TestTemplate testTemplate) {
        this.testTemplate = testTemplate;
    }
    
    public TestStatus getStatus() {
        return status;
    }
    
    public void setStatus(TestStatus status) {
        this.status = status;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public JsonNode getResults() {
        return results;
    }
    
    public void setResults(JsonNode results) {
        this.results = results;
    }
    
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
        if (approved != null && approved) {
            this.approvedAt = LocalDateTime.now();
        }
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getResultsEnteredAt() {
        return resultsEnteredAt;
    }

    public void setResultsEnteredAt(LocalDateTime resultsEnteredAt) {
        this.resultsEnteredAt = resultsEnteredAt;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public LabEquipment getEquipment() {
        return equipment;
    }

    public void setEquipment(LabEquipment equipment) {
        this.equipment = equipment;
    }

    public String getMachineUsed() {
        return machineUsed;
    }

    public void setMachineUsed(String machineUsed) {
        this.machineUsed = machineUsed;
    }

    public LocalDateTime getTestStartedAt() {
        return testStartedAt;
    }

    public void setTestStartedAt(LocalDateTime testStartedAt) {
        this.testStartedAt = testStartedAt;
    }

    public LocalDateTime getTestCompletedAt() {
        return testCompletedAt;
    }

    public void setTestCompletedAt(LocalDateTime testCompletedAt) {
        this.testCompletedAt = testCompletedAt;
    }

    /**
     * Check if sample is collected and ready for testing
     */
    public boolean isSampleReadyForTesting() {
        return sample != null && sample.getStatus() != null &&
               sample.getStatus().isAvailableForTesting();
    }

    /**
     * Check if test can be started (sample collected and accepted)
     */
    public boolean canStartTest() {
        return isSampleReadyForTesting() &&
               (status == TestStatus.PENDING || status == TestStatus.IN_PROGRESS);
    }
}
