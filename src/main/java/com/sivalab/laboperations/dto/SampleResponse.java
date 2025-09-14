package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.SampleStatus;
import com.sivalab.laboperations.entity.SampleType;

import java.time.LocalDateTime;

public class SampleResponse {
    
    private Long sampleId;
    private Long testId;
    private Long visitId;
    private String patientName;
    private String testName;
    private SampleType sampleType;
    private SampleStatus status;
    private LocalDateTime collectedAt;
    private String collectedBy;
    private String collectionSite;
    private JsonNode collectionConditions;
    private LocalDateTime receivedAt;
    private String receivedBy;
    private String receiptCondition;
    private Double volumeReceived;
    private String containerType;
    private String preservative;
    private Boolean rejected;
    private String rejectionReason;
    private String notes;
    
    // Constructors
    public SampleResponse() {}
    
    public SampleResponse(Long sampleId, Long testId, Long visitId, String patientName, 
                         String testName, SampleType sampleType, SampleStatus status) {
        this.sampleId = sampleId;
        this.testId = testId;
        this.visitId = visitId;
        this.patientName = patientName;
        this.testName = testName;
        this.sampleType = sampleType;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getSampleId() {
        return sampleId;
    }
    
    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }
    
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
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public SampleType getSampleType() {
        return sampleType;
    }
    
    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
    }
    
    public SampleStatus getStatus() {
        return status;
    }
    
    public void setStatus(SampleStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }
    
    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }
    
    public String getCollectedBy() {
        return collectedBy;
    }
    
    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }
    
    public String getCollectionSite() {
        return collectionSite;
    }
    
    public void setCollectionSite(String collectionSite) {
        this.collectionSite = collectionSite;
    }
    
    public JsonNode getCollectionConditions() {
        return collectionConditions;
    }
    
    public void setCollectionConditions(JsonNode collectionConditions) {
        this.collectionConditions = collectionConditions;
    }
    
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
    
    public String getReceivedBy() {
        return receivedBy;
    }
    
    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }
    
    public String getReceiptCondition() {
        return receiptCondition;
    }
    
    public void setReceiptCondition(String receiptCondition) {
        this.receiptCondition = receiptCondition;
    }
    
    public Double getVolumeReceived() {
        return volumeReceived;
    }
    
    public void setVolumeReceived(Double volumeReceived) {
        this.volumeReceived = volumeReceived;
    }
    
    public String getContainerType() {
        return containerType;
    }
    
    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }
    
    public String getPreservative() {
        return preservative;
    }
    
    public void setPreservative(String preservative) {
        this.preservative = preservative;
    }
    
    public Boolean getRejected() {
        return rejected;
    }
    
    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
