package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.VisitStatus;

import java.time.LocalDateTime;
import java.util.List;

public class VisitResponse {
    
    private Long visitId;
    private JsonNode patientDetails;
    private LocalDateTime createdAt;
    private VisitStatus status;
    private List<LabTestResponse> labTests;
    private BillingResponse billing;
    
    // Constructors
    public VisitResponse() {}
    
    public VisitResponse(Long visitId, JsonNode patientDetails, LocalDateTime createdAt, VisitStatus status) {
        this.visitId = visitId;
        this.patientDetails = patientDetails;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getVisitId() {
        return visitId;
    }
    
    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
    
    public JsonNode getPatientDetails() {
        return patientDetails;
    }
    
    public void setPatientDetails(JsonNode patientDetails) {
        this.patientDetails = patientDetails;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public VisitStatus getStatus() {
        return status;
    }
    
    public void setStatus(VisitStatus status) {
        this.status = status;
    }
    
    public List<LabTestResponse> getLabTests() {
        return labTests;
    }
    
    public void setLabTests(List<LabTestResponse> labTests) {
        this.labTests = labTests;
    }
    
    public BillingResponse getBilling() {
        return billing;
    }
    
    public void setBilling(BillingResponse billing) {
        this.billing = billing;
    }
}
