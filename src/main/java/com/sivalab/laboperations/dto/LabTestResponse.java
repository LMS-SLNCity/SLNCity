package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.TestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LabTestResponse {
    
    private Long testId;
    private Long visitId;
    private TestTemplateResponse testTemplate;
    private TestStatus status;
    private BigDecimal price;
    private JsonNode results;
    private Boolean approved;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime expectedCompletionTime;
    
    // Constructors
    public LabTestResponse() {}
    
    // Getters and Setters
    public Long getTestId() {
        return testId;
    }
    
    public void setTestId(Long testId) {
        this.testId = testId;
    }
    
    public Long getVisitId() {
        return visitId;
    }
    
    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
    
    public TestTemplateResponse getTestTemplate() {
        return testTemplate;
    }
    
    public void setTestTemplate(TestTemplateResponse testTemplate) {
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

    public LocalDateTime getExpectedCompletionTime() {
        return expectedCompletionTime;
    }

    public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) {
        this.expectedCompletionTime = expectedCompletionTime;
    }
}
