package com.sivalab.laboperations.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.Sample;
import com.sivalab.laboperations.entity.SampleStatus;
import com.sivalab.laboperations.entity.SampleType;
import com.sivalab.laboperations.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * NABL-compliant Sample Management Controller
 * Implements complete sample lifecycle tracking according to NABL 112 requirements
 */
@RestController
@RequestMapping("/samples")
@CrossOrigin(origins = "*")
public class SampleController {
    
    private final SampleService sampleService;
    
    @Autowired
    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }
    
    /**
     * NABL Phase 1: Sample Collection
     * POST /samples/collect
     */
    @PostMapping("/collect")
    public ResponseEntity<Sample> collectSample(@RequestBody CollectSampleRequest request) {
        try {
            Sample sample = sampleService.collectSample(
                request.getVisitId(),
                request.getSampleType(),
                request.getCollectedBy(),
                request.getCollectionSite(),
                request.getCollectionConditions()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 2: Sample Receipt
     * PATCH /samples/{sampleNumber}/receive
     */
    @PatchMapping("/{sampleNumber}/receive")
    public ResponseEntity<Sample> receiveSample(@PathVariable String sampleNumber,
                                               @RequestBody ReceiveSampleRequest request) {
        try {
            Sample sample = sampleService.receiveSample(
                sampleNumber,
                request.getReceivedBy(),
                request.getReceiptTemperature(),
                request.getReceiptCondition()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 3: Sample Acceptance/Rejection
     * PATCH /samples/{sampleNumber}/accept
     */
    @PatchMapping("/{sampleNumber}/accept")
    public ResponseEntity<Sample> acceptSample(@PathVariable String sampleNumber,
                                              @RequestBody AcceptSampleRequest request) {
        try {
            Sample sample = sampleService.acceptSample(
                sampleNumber,
                request.getAcceptedBy(),
                request.getVolumeReceived(),
                request.getContainerType(),
                request.getPreservative()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 3: Sample Rejection
     * PATCH /samples/{sampleNumber}/reject
     */
    @PatchMapping("/{sampleNumber}/reject")
    public ResponseEntity<Sample> rejectSample(@PathVariable String sampleNumber,
                                              @RequestBody RejectSampleRequest request) {
        try {
            Sample sample = sampleService.rejectSample(
                sampleNumber,
                request.getRejectedBy(),
                request.getRejectionReason()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 4: Sample Processing
     * PATCH /samples/{sampleNumber}/process
     */
    @PatchMapping("/{sampleNumber}/process")
    public ResponseEntity<Sample> startProcessing(@PathVariable String sampleNumber,
                                                 @RequestBody ProcessSampleRequest request) {
        try {
            Sample sample = sampleService.startProcessing(
                sampleNumber,
                request.getProcessedBy(),
                request.getStorageLocation(),
                request.getStorageTemperature()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 5: Sample Analysis
     * PATCH /samples/{sampleNumber}/analyze
     */
    @PatchMapping("/{sampleNumber}/analyze")
    public ResponseEntity<Sample> startAnalysis(@PathVariable String sampleNumber,
                                               @RequestBody AnalyzeSampleRequest request) {
        try {
            Sample sample = sampleService.startAnalysis(sampleNumber, request.getAnalyst());
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 6: Analysis Completion
     * PATCH /samples/{sampleNumber}/complete
     */
    @PatchMapping("/{sampleNumber}/complete")
    public ResponseEntity<Sample> completeAnalysis(@PathVariable String sampleNumber,
                                                  @RequestBody CompleteAnalysisRequest request) {
        try {
            Sample sample = sampleService.completeAnalysis(
                sampleNumber,
                request.getAnalyst(),
                request.getQualityIndicators()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 7: Result Review
     * PATCH /samples/{sampleNumber}/review
     */
    @PatchMapping("/{sampleNumber}/review")
    public ResponseEntity<Sample> reviewSample(@PathVariable String sampleNumber,
                                              @RequestBody ReviewSampleRequest request) {
        try {
            Sample sample = sampleService.reviewSample(sampleNumber, request.getReviewer());
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 8: Sample Storage
     * PATCH /samples/{sampleNumber}/store
     */
    @PatchMapping("/{sampleNumber}/store")
    public ResponseEntity<Sample> storeSample(@PathVariable String sampleNumber,
                                             @RequestBody StoreSampleRequest request) {
        try {
            Sample sample = sampleService.storeSample(
                sampleNumber,
                request.getStorageLocation(),
                request.getStorageTemperature(),
                request.getStorageConditions()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * NABL Phase 9: Sample Disposal
     * PATCH /samples/{sampleNumber}/dispose
     */
    @PatchMapping("/{sampleNumber}/dispose")
    public ResponseEntity<Sample> disposeSample(@PathVariable String sampleNumber,
                                               @RequestBody DisposeSampleRequest request) {
        try {
            Sample sample = sampleService.disposeSample(
                sampleNumber,
                request.getDisposedBy(),
                request.getDisposalMethod(),
                request.getDisposalBatch()
            );
            return ResponseEntity.ok(sample);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get sample by number (with complete chain of custody)
     * GET /samples/{sampleNumber}
     */
    @GetMapping("/{sampleNumber}")
    public ResponseEntity<Sample> getSample(@PathVariable String sampleNumber) {
        Optional<Sample> sample = sampleService.getSampleByNumber(sampleNumber);
        return sample.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all samples for a visit
     * GET /samples/visit/{visitId}
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<Sample>> getSamplesForVisit(@PathVariable Long visitId) {
        List<Sample> samples = sampleService.getSamplesForVisit(visitId);
        return ResponseEntity.ok(samples);
    }
    
    /**
     * Get samples by status
     * GET /samples/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Sample>> getSamplesByStatus(@PathVariable SampleStatus status) {
        List<Sample> samples = sampleService.getSamplesByStatus(status);
        return ResponseEntity.ok(samples);
    }
    
    // Request DTOs
    public static class CollectSampleRequest {
        private Long visitId;
        private SampleType sampleType;
        private String collectedBy;
        private String collectionSite;
        private JsonNode collectionConditions;
        
        // Getters and setters
        public Long getVisitId() { return visitId; }
        public void setVisitId(Long visitId) { this.visitId = visitId; }
        public SampleType getSampleType() { return sampleType; }
        public void setSampleType(SampleType sampleType) { this.sampleType = sampleType; }
        public String getCollectedBy() { return collectedBy; }
        public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }
        public String getCollectionSite() { return collectionSite; }
        public void setCollectionSite(String collectionSite) { this.collectionSite = collectionSite; }
        public JsonNode getCollectionConditions() { return collectionConditions; }
        public void setCollectionConditions(JsonNode collectionConditions) { this.collectionConditions = collectionConditions; }
    }
    
    public static class ReceiveSampleRequest {
        private String receivedBy;
        private Double receiptTemperature;
        private String receiptCondition;
        
        public String getReceivedBy() { return receivedBy; }
        public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
        public Double getReceiptTemperature() { return receiptTemperature; }
        public void setReceiptTemperature(Double receiptTemperature) { this.receiptTemperature = receiptTemperature; }
        public String getReceiptCondition() { return receiptCondition; }
        public void setReceiptCondition(String receiptCondition) { this.receiptCondition = receiptCondition; }
    }
    
    public static class AcceptSampleRequest {
        private String acceptedBy;
        private Double volumeReceived;
        private String containerType;
        private String preservative;
        
        public String getAcceptedBy() { return acceptedBy; }
        public void setAcceptedBy(String acceptedBy) { this.acceptedBy = acceptedBy; }
        public Double getVolumeReceived() { return volumeReceived; }
        public void setVolumeReceived(Double volumeReceived) { this.volumeReceived = volumeReceived; }
        public String getContainerType() { return containerType; }
        public void setContainerType(String containerType) { this.containerType = containerType; }
        public String getPreservative() { return preservative; }
        public void setPreservative(String preservative) { this.preservative = preservative; }
    }
    
    public static class RejectSampleRequest {
        private String rejectedBy;
        private String rejectionReason;
        
        public String getRejectedBy() { return rejectedBy; }
        public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
    
    public static class ProcessSampleRequest {
        private String processedBy;
        private String storageLocation;
        private Double storageTemperature;

        public String getProcessedBy() { return processedBy; }
        public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
        public String getStorageLocation() { return storageLocation; }
        public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
        public Double getStorageTemperature() { return storageTemperature; }
        public void setStorageTemperature(Double storageTemperature) { this.storageTemperature = storageTemperature; }
    }

    public static class AnalyzeSampleRequest {
        private String analyst;

        public String getAnalyst() { return analyst; }
        public void setAnalyst(String analyst) { this.analyst = analyst; }
    }

    public static class CompleteAnalysisRequest {
        private String analyst;
        private JsonNode qualityIndicators;

        public String getAnalyst() { return analyst; }
        public void setAnalyst(String analyst) { this.analyst = analyst; }
        public JsonNode getQualityIndicators() { return qualityIndicators; }
        public void setQualityIndicators(JsonNode qualityIndicators) { this.qualityIndicators = qualityIndicators; }
    }

    public static class ReviewSampleRequest {
        private String reviewer;

        public String getReviewer() { return reviewer; }
        public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    }

    public static class StoreSampleRequest {
        private String storageLocation;
        private Double storageTemperature;
        private String storageConditions;

        public String getStorageLocation() { return storageLocation; }
        public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
        public Double getStorageTemperature() { return storageTemperature; }
        public void setStorageTemperature(Double storageTemperature) { this.storageTemperature = storageTemperature; }
        public String getStorageConditions() { return storageConditions; }
        public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
    }

    public static class DisposeSampleRequest {
        private String disposedBy;
        private String disposalMethod;
        private String disposalBatch;

        public String getDisposedBy() { return disposedBy; }
        public void setDisposedBy(String disposedBy) { this.disposedBy = disposedBy; }
        public String getDisposalMethod() { return disposalMethod; }
        public void setDisposalMethod(String disposalMethod) { this.disposalMethod = disposalMethod; }
        public String getDisposalBatch() { return disposalBatch; }
        public void setDisposalBatch(String disposalBatch) { this.disposalBatch = disposalBatch; }
    }
}
