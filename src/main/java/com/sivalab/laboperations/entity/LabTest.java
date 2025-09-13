package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

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
    
    @DecimalMin(value = "0.01", inclusive = false, message = "Price must be greater than zero")
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
}
