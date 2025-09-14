package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.SampleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SampleCollectionRequest {
    
    @NotNull(message = "Sample type is required")
    private SampleType sampleType;
    
    @NotBlank(message = "Collected by is required")
    private String collectedBy;
    
    private String collectionSite;
    
    private JsonNode collectionConditions;
    
    private Double volumeReceived;
    
    private String containerType;
    
    private String preservative;
    
    private String notes;
    
    // Constructors
    public SampleCollectionRequest() {}
    
    public SampleCollectionRequest(SampleType sampleType, String collectedBy) {
        this.sampleType = sampleType;
        this.collectedBy = collectedBy;
    }
    
    // Getters and Setters
    public SampleType getSampleType() {
        return sampleType;
    }
    
    public void setSampleType(SampleType sampleType) {
        this.sampleType = sampleType;
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
