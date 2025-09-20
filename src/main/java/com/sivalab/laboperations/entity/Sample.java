package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * NABL-compliant Sample entity for complete sample lifecycle tracking
 * Covers: Collection → Receipt → Processing → Storage → Analysis → Disposal
 */
@Entity
@Table(name = "samples")
public class Sample {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_id")
    private Long sampleId;
    
    @Column(name = "sample_number", unique = true, nullable = false)
    private String sampleNumber; // NABL requirement: Unique sample identification
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Visit visit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id")
    @JsonIgnoreProperties({"sample", "hibernateLazyInitializer", "handler"})
    private LabTest labTest;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false)
    private SampleType sampleType; // Blood, Urine, Serum, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SampleStatus status = SampleStatus.COLLECTED;
    
    // NABL Collection Requirements
    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;
    
    @Column(name = "collected_by", nullable = false)
    private String collectedBy;
    
    @Column(name = "collection_site")
    private String collectionSite;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "collection_conditions", columnDefinition = "json")
    private JsonNode collectionConditions; // Fasting, time, special conditions
    
    // NABL Receipt Requirements
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "received_by")
    private String receivedBy;
    
    @Column(name = "receipt_temperature")
    private Double receiptTemperature;
    
    @Column(name = "receipt_condition")
    private String receiptCondition; // Good, Hemolyzed, Clotted, etc.
    
    // NABL Processing Requirements
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;
    
    @Column(name = "processed_by")
    private String processedBy;
    
    // NABL Storage Requirements
    @Column(name = "storage_location")
    private String storageLocation;
    
    @Column(name = "storage_temperature")
    private Double storageTemperature;
    
    @Column(name = "storage_conditions")
    private String storageConditions;
    
    // NABL Chain of Custody
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chain_of_custody", columnDefinition = "json")
    private JsonNode chainOfCustody;
    
    // NABL Quality Control
    @Column(name = "volume_received")
    private Double volumeReceived;
    
    @Column(name = "volume_required")
    private Double volumeRequired;
    
    @Column(name = "container_type")
    private String containerType;
    
    @Column(name = "preservative")
    private String preservative;
    
    // NABL Rejection Criteria
    @Column(name = "rejected")
    private Boolean rejected = false;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "rejected_by")
    private String rejectedBy;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    // NABL Disposal Requirements
    @Column(name = "disposed_at")
    private LocalDateTime disposedAt;
    
    @Column(name = "disposed_by")
    private String disposedBy;
    
    @Column(name = "disposal_method")
    private String disposalMethod;
    
    @Column(name = "disposal_batch")
    private String disposalBatch;
    
    // NABL Comments and Notes
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality_indicators", columnDefinition = "json")
    private JsonNode qualityIndicators; // pH, appearance, etc.
    
    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Sample() {}
    
    public Sample(String sampleNumber, Visit visit, SampleType sampleType, 
                  String collectedBy, LocalDateTime collectedAt) {
        this.sampleNumber = sampleNumber;
        this.visit = visit;
        this.sampleType = sampleType;
        this.collectedBy = collectedBy;
        this.collectedAt = collectedAt;
    }
    
    // Getters and Setters
    public Long getSampleId() { return sampleId; }
    public void setSampleId(Long sampleId) { this.sampleId = sampleId; }
    
    public String getSampleNumber() { return sampleNumber; }
    public void setSampleNumber(String sampleNumber) { this.sampleNumber = sampleNumber; }
    
    public Visit getVisit() { return visit; }
    public void setVisit(Visit visit) { this.visit = visit; }
    
    public SampleType getSampleType() { return sampleType; }
    public void setSampleType(SampleType sampleType) { this.sampleType = sampleType; }
    
    public SampleStatus getStatus() { return status; }
    public void setStatus(SampleStatus status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    
    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }
    
    public String getCollectionSite() { return collectionSite; }
    public void setCollectionSite(String collectionSite) { this.collectionSite = collectionSite; }
    
    public JsonNode getCollectionConditions() { return collectionConditions; }
    public void setCollectionConditions(JsonNode collectionConditions) { this.collectionConditions = collectionConditions; }
    
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    
    public Double getReceiptTemperature() { return receiptTemperature; }
    public void setReceiptTemperature(Double receiptTemperature) { this.receiptTemperature = receiptTemperature; }
    
    public String getReceiptCondition() { return receiptCondition; }
    public void setReceiptCondition(String receiptCondition) { this.receiptCondition = receiptCondition; }
    
    public LocalDateTime getProcessingStartedAt() { return processingStartedAt; }
    public void setProcessingStartedAt(LocalDateTime processingStartedAt) { this.processingStartedAt = processingStartedAt; }
    
    public LocalDateTime getProcessingCompletedAt() { return processingCompletedAt; }
    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) { this.processingCompletedAt = processingCompletedAt; }
    
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public Double getStorageTemperature() { return storageTemperature; }
    public void setStorageTemperature(Double storageTemperature) { this.storageTemperature = storageTemperature; }
    
    public String getStorageConditions() { return storageConditions; }
    public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
    
    public JsonNode getChainOfCustody() { return chainOfCustody; }
    public void setChainOfCustody(JsonNode chainOfCustody) { this.chainOfCustody = chainOfCustody; }
    
    public Double getVolumeReceived() { return volumeReceived; }
    public void setVolumeReceived(Double volumeReceived) { this.volumeReceived = volumeReceived; }
    
    public Double getVolumeRequired() { return volumeRequired; }
    public void setVolumeRequired(Double volumeRequired) { this.volumeRequired = volumeRequired; }
    
    public String getContainerType() { return containerType; }
    public void setContainerType(String containerType) { this.containerType = containerType; }
    
    public String getPreservative() { return preservative; }
    public void setPreservative(String preservative) { this.preservative = preservative; }
    
    public Boolean getRejected() { return rejected; }
    public void setRejected(Boolean rejected) { this.rejected = rejected; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
    
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    
    public LocalDateTime getDisposedAt() { return disposedAt; }
    public void setDisposedAt(LocalDateTime disposedAt) { this.disposedAt = disposedAt; }
    
    public String getDisposedBy() { return disposedBy; }
    public void setDisposedBy(String disposedBy) { this.disposedBy = disposedBy; }
    
    public String getDisposalMethod() { return disposalMethod; }
    public void setDisposalMethod(String disposalMethod) { this.disposalMethod = disposalMethod; }
    
    public String getDisposalBatch() { return disposalBatch; }
    public void setDisposalBatch(String disposalBatch) { this.disposalBatch = disposalBatch; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public JsonNode getQualityIndicators() { return qualityIndicators; }
    public void setQualityIndicators(JsonNode qualityIndicators) { this.qualityIndicators = qualityIndicators; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LabTest getLabTest() { return labTest; }
    public void setLabTest(LabTest labTest) { this.labTest = labTest; }
}
