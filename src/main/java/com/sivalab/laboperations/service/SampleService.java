package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.SampleRepository;
import com.sivalab.laboperations.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * NABL-compliant Sample Management Service
 * Implements complete sample lifecycle tracking according to NABL 112 requirements
 */
@Service
@Transactional
public class SampleService {
    
    private final SampleRepository sampleRepository;
    private final VisitRepository visitRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public SampleService(SampleRepository sampleRepository, 
                        VisitRepository visitRepository,
                        ObjectMapper objectMapper) {
        this.sampleRepository = sampleRepository;
        this.visitRepository = visitRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * NABL Requirement: Sample Collection Documentation
     * Create new sample with complete collection documentation
     */
    public Sample collectSample(Long visitId, SampleType sampleType, String collectedBy, 
                               String collectionSite, JsonNode collectionConditions) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));
        
        // Generate unique sample number (NABL requirement)
        String sampleNumber = generateSampleNumber(visitId, sampleType);
        
        Sample sample = new Sample(sampleNumber, visit, sampleType, collectedBy, LocalDateTime.now());
        sample.setCollectionSite(collectionSite);
        sample.setCollectionConditions(collectionConditions);
        sample.setStatus(SampleStatus.COLLECTED);
        
        // Initialize chain of custody
        sample.setChainOfCustody(createInitialChainOfCustody(collectedBy));
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Receipt Documentation
     * Record sample receipt with quality checks
     */
    public Sample receiveSample(String sampleNumber, String receivedBy, 
                               Double receiptTemperature, String receiptCondition) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        // Validate status transition
        if (!sample.getStatus().canTransitionTo(SampleStatus.RECEIVED)) {
            throw new RuntimeException("Invalid status transition from " + sample.getStatus() + " to RECEIVED");
        }
        
        sample.setReceivedAt(LocalDateTime.now());
        sample.setReceivedBy(receivedBy);
        sample.setReceiptTemperature(receiptTemperature);
        sample.setReceiptCondition(receiptCondition);
        sample.setStatus(SampleStatus.RECEIVED);
        
        // Update chain of custody
        updateChainOfCustody(sample, "RECEIVED", receivedBy, "Sample received at laboratory");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Acceptance/Rejection
     * Perform quality control checks and accept or reject sample
     */
    public Sample acceptSample(String sampleNumber, String acceptedBy, 
                              Double volumeReceived, String containerType, String preservative) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        // Perform quality checks
        boolean qualityAcceptable = performQualityChecks(sample, volumeReceived);
        
        if (qualityAcceptable) {
            sample.setStatus(SampleStatus.ACCEPTED);
            sample.setVolumeReceived(volumeReceived);
            sample.setContainerType(containerType);
            sample.setPreservative(preservative);
            
            // Set volume requirements based on sample type
            sample.setVolumeRequired(sample.getSampleType().getMinimumVolume());
            
            updateChainOfCustody(sample, "ACCEPTED", acceptedBy, "Sample accepted for testing");
        } else {
            rejectSample(sampleNumber, acceptedBy, "Failed quality control checks");
        }
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Rejection Documentation
     * Reject sample with proper documentation
     */
    public Sample rejectSample(String sampleNumber, String rejectedBy, String rejectionReason) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        sample.setStatus(SampleStatus.REJECTED);
        sample.setRejected(true);
        sample.setRejectedBy(rejectedBy);
        sample.setRejectedAt(LocalDateTime.now());
        sample.setRejectionReason(rejectionReason);
        
        updateChainOfCustody(sample, "REJECTED", rejectedBy, "Sample rejected: " + rejectionReason);
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Processing Documentation
     * Start sample processing with documentation
     */
    public Sample startProcessing(String sampleNumber, String processedBy, 
                                 String storageLocation, Double storageTemperature) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        if (!sample.getStatus().canTransitionTo(SampleStatus.PROCESSING)) {
            throw new RuntimeException("Invalid status transition from " + sample.getStatus() + " to PROCESSING");
        }
        
        sample.setStatus(SampleStatus.PROCESSING);
        sample.setProcessingStartedAt(LocalDateTime.now());
        sample.setProcessedBy(processedBy);
        sample.setStorageLocation(storageLocation);
        sample.setStorageTemperature(storageTemperature);
        sample.setStorageConditions(sample.getSampleType().getStorageTemperature());
        
        updateChainOfCustody(sample, "PROCESSING", processedBy, "Sample processing started");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Analysis Documentation
     * Mark sample as in analysis
     */
    public Sample startAnalysis(String sampleNumber, String analyst) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        if (!sample.getStatus().canTransitionTo(SampleStatus.IN_ANALYSIS)) {
            throw new RuntimeException("Invalid status transition from " + sample.getStatus() + " to IN_ANALYSIS");
        }
        
        sample.setStatus(SampleStatus.IN_ANALYSIS);
        updateChainOfCustody(sample, "IN_ANALYSIS", analyst, "Sample analysis started");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Analysis Completion Documentation
     * Complete analysis and move to review
     */
    public Sample completeAnalysis(String sampleNumber, String analyst, JsonNode qualityIndicators) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        sample.setStatus(SampleStatus.ANALYSIS_COMPLETE);
        sample.setProcessingCompletedAt(LocalDateTime.now());
        sample.setQualityIndicators(qualityIndicators);
        
        updateChainOfCustody(sample, "ANALYSIS_COMPLETE", analyst, "Sample analysis completed");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Result Review Documentation
     * Review and approve results
     */
    public Sample reviewSample(String sampleNumber, String reviewer) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        sample.setStatus(SampleStatus.REVIEWED);
        updateChainOfCustody(sample, "REVIEWED", reviewer, "Results reviewed and approved");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Storage Documentation
     * Store sample for retention period
     */
    public Sample storeSample(String sampleNumber, String storageLocation, 
                             Double storageTemperature, String storageConditions) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        sample.setStatus(SampleStatus.STORED);
        sample.setStorageLocation(storageLocation);
        sample.setStorageTemperature(storageTemperature);
        sample.setStorageConditions(storageConditions);
        
        updateChainOfCustody(sample, "STORED", "System", "Sample stored for retention");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * NABL Requirement: Sample Disposal Documentation
     * Dispose sample according to protocols
     */
    public Sample disposeSample(String sampleNumber, String disposedBy, 
                               String disposalMethod, String disposalBatch) {
        Sample sample = sampleRepository.findBySampleNumber(sampleNumber)
                .orElseThrow(() -> new RuntimeException("Sample not found: " + sampleNumber));
        
        sample.setStatus(SampleStatus.DISPOSED);
        sample.setDisposedAt(LocalDateTime.now());
        sample.setDisposedBy(disposedBy);
        sample.setDisposalMethod(disposalMethod);
        sample.setDisposalBatch(disposalBatch);
        
        updateChainOfCustody(sample, "DISPOSED", disposedBy, 
                           "Sample disposed via " + disposalMethod + " (Batch: " + disposalBatch + ")");
        
        return sampleRepository.save(sample);
    }
    
    /**
     * Get sample by number
     */
    @Transactional(readOnly = true)
    public Optional<Sample> getSampleByNumber(String sampleNumber) {
        return sampleRepository.findBySampleNumber(sampleNumber);
    }
    
    /**
     * Get all samples for a visit
     */
    @Transactional(readOnly = true)
    public List<Sample> getSamplesForVisit(Long visitId) {
        return sampleRepository.findByVisitVisitId(visitId);
    }
    
    /**
     * Get samples by status
     */
    @Transactional(readOnly = true)
    public List<Sample> getSamplesByStatus(SampleStatus status) {
        return sampleRepository.findByStatus(status);
    }
    
    /**
     * Generate unique sample number (NABL requirement)
     */
    private String generateSampleNumber(Long visitId, SampleType sampleType) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String typeCode = sampleType.getCode();
        
        // Find next sequence number for today
        String basePattern = datePrefix + "-" + typeCode + "-";
        long count = sampleRepository.findAll().stream()
                .filter(s -> s.getSampleNumber().startsWith(basePattern))
                .count();
        
        return String.format("%s%s-%s-%04d", datePrefix, typeCode, visitId, count + 1);
    }
    
    /**
     * Create initial chain of custody record
     */
    private JsonNode createInitialChainOfCustody(String collectedBy) {
        ObjectNode custody = objectMapper.createObjectNode();
        ArrayNode events = objectMapper.createArrayNode();
        
        ObjectNode collectionEvent = objectMapper.createObjectNode();
        collectionEvent.put("timestamp", LocalDateTime.now().toString());
        collectionEvent.put("event", "COLLECTED");
        collectionEvent.put("person", collectedBy);
        collectionEvent.put("description", "Sample collected from patient");
        
        events.add(collectionEvent);
        custody.set("events", events);
        
        return custody;
    }
    
    /**
     * Update chain of custody
     */
    private void updateChainOfCustody(Sample sample, String event, String person, String description) {
        ObjectNode custody = (ObjectNode) sample.getChainOfCustody();
        if (custody == null) {
            custody = objectMapper.createObjectNode();
            custody.set("events", objectMapper.createArrayNode());
        }
        
        ArrayNode events = (ArrayNode) custody.get("events");
        
        ObjectNode newEvent = objectMapper.createObjectNode();
        newEvent.put("timestamp", LocalDateTime.now().toString());
        newEvent.put("event", event);
        newEvent.put("person", person);
        newEvent.put("description", description);
        
        events.add(newEvent);
        sample.setChainOfCustody(custody);
    }
    
    /**
     * Perform quality control checks
     */
    private boolean performQualityChecks(Sample sample, Double volumeReceived) {
        // Check minimum volume
        if (volumeReceived < sample.getSampleType().getMinimumVolume()) {
            return false;
        }
        
        // Check receipt condition
        if ("Hemolyzed".equals(sample.getReceiptCondition()) || 
            "Clotted".equals(sample.getReceiptCondition()) ||
            "Contaminated".equals(sample.getReceiptCondition())) {
            return false;
        }
        
        // Check temperature if applicable
        if (sample.getSampleType().requiresRefrigeration() && 
            sample.getReceiptTemperature() != null &&
            (sample.getReceiptTemperature() < 2.0 || sample.getReceiptTemperature() > 8.0)) {
            return false;
        }
        
        return true;
    }

    /**
     * Get all samples
     */
    public List<Sample> getAllSamples() {
        return sampleRepository.findAll();
    }
}
