package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.dto.SampleCollectionRequest;
import com.sivalab.laboperations.dto.SampleResponse;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.LabTestRepository;
import com.sivalab.laboperations.repository.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SampleCollectionService {
    
    private final SampleRepository sampleRepository;
    private final LabTestRepository labTestRepository;
    
    @Autowired
    public SampleCollectionService(SampleRepository sampleRepository, LabTestRepository labTestRepository) {
        this.sampleRepository = sampleRepository;
        this.labTestRepository = labTestRepository;
    }
    
    /**
     * Get all tests pending sample collection
     */
    @Transactional(readOnly = true)
    public List<SampleResponse> getPendingSamples() {
        // Get all tests that don't have samples collected yet
        List<LabTest> testsWithoutSamples = labTestRepository.findTestsWithoutSamples();
        
        return testsWithoutSamples.stream()
                .map(this::convertToSampleResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Collect sample for a test
     */
    public SampleResponse collectSample(Long testId, SampleCollectionRequest request) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found with ID: " + testId));
        
        if (labTest.getSample() != null) {
            throw new RuntimeException("Sample already collected for this test");
        }
        
        // Create new sample
        Sample sample = new Sample();
        sample.setLabTest(labTest);
        sample.setVisit(labTest.getVisit()); // Set visit from lab test
        sample.setSampleNumber(generateSampleNumber()); // Generate unique sample number
        sample.setSampleType(request.getSampleType());
        sample.setStatus(SampleStatus.COLLECTED);
        sample.setCollectedAt(LocalDateTime.now());
        sample.setCollectedBy(request.getCollectedBy());
        sample.setCollectionSite(request.getCollectionSite());
        sample.setCollectionConditions(request.getCollectionConditions());
        sample.setVolumeReceived(request.getVolumeReceived());
        sample.setContainerType(request.getContainerType());
        sample.setPreservative(request.getPreservative());
        
        sample = sampleRepository.save(sample);
        
        // Update lab test with sample and change status
        labTest.setSample(sample);
        labTest.setStatus(TestStatus.PENDING); // Sample collected and ready for lab processing
        labTestRepository.save(labTest);

        System.out.println("✅ WORKFLOW FIX: Lab Test " + labTest.getTestId() + " status updated to PENDING - sample collected and ready for lab processing");
        
        return convertToSampleResponse(labTest);
    }
    
    /**
     * Update sample status
     */
    public SampleResponse updateSampleStatus(Long sampleId, String statusString) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Sample not found with ID: " + sampleId));
        
        try {
            SampleStatus newStatus = SampleStatus.valueOf(statusString.toUpperCase());
            
            // Validate status transition
            if (!isValidStatusTransition(sample.getStatus(), newStatus)) {
                throw new RuntimeException("Invalid status transition from " + sample.getStatus() + " to " + newStatus);
            }
            
            sample.setStatus(newStatus);
            
            // Update timestamps based on status
            switch (newStatus) {
                case RECEIVED:
                    sample.setReceivedAt(LocalDateTime.now());
                    break;
                case ACCEPTED:
                    // Sample is now ready for testing
                    if (sample.getLabTest() != null) {
                        sample.getLabTest().setStatus(TestStatus.PENDING);
                        labTestRepository.save(sample.getLabTest());
                    }
                    break;
                case REJECTED:
                    sample.setRejected(true);
                    if (sample.getLabTest() != null) {
                        sample.getLabTest().setStatus(TestStatus.PENDING); // Need to recollect
                        labTestRepository.save(sample.getLabTest());
                    }
                    break;
            }
            
            sample = sampleRepository.save(sample);
            return convertToSampleResponse(sample.getLabTest());
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + statusString);
        }
    }
    
    /**
     * Get sample by ID
     */
    @Transactional(readOnly = true)
    public SampleResponse getSample(Long sampleId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new RuntimeException("Sample not found with ID: " + sampleId));
        
        return convertToSampleResponse(sample.getLabTest());
    }
    
    /**
     * Get all samples for a visit
     */
    @Transactional(readOnly = true)
    public List<SampleResponse> getSamplesByVisit(Long visitId) {
        List<Sample> samples = sampleRepository.findByLabTestVisitVisitId(visitId);

        return samples.stream()
                .map(sample -> {
                    LabTest labTest = sample.getLabTest();
                    return convertToSampleResponse(labTest);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Convert LabTest to SampleResponse
     */
    private SampleResponse convertToSampleResponse(LabTest labTest) {
        SampleResponse response = new SampleResponse();
        
        response.setTestId(labTest.getTestId());
        response.setVisitId(labTest.getVisit().getVisitId());
        
        // Patient information
        if (labTest.getVisit().getPatientDetails() != null) {
            JsonNode patientDetails = labTest.getVisit().getPatientDetails();
            String firstName = patientDetails.has("firstName") ? patientDetails.get("firstName").asText() : "";
            String lastName = patientDetails.has("lastName") ? patientDetails.get("lastName").asText() : "";
            response.setPatientName(firstName + " " + lastName);
        }
        
        // Test information
        if (labTest.getTestTemplate() != null) {
            response.setTestName(labTest.getTestTemplate().getName());
        }
        
        // Sample information
        Sample sample = labTest.getSample();
        if (sample != null) {
            response.setSampleId(sample.getSampleId());
            response.setSampleType(sample.getSampleType());
            response.setStatus(sample.getStatus());
            response.setCollectedAt(sample.getCollectedAt());
            response.setCollectedBy(sample.getCollectedBy());
            response.setCollectionSite(sample.getCollectionSite());
            response.setCollectionConditions(sample.getCollectionConditions());
            response.setReceivedAt(sample.getReceivedAt());
            response.setReceivedBy(sample.getReceivedBy());
            response.setReceiptCondition(sample.getReceiptCondition());
            response.setVolumeReceived(sample.getVolumeReceived());
            response.setContainerType(sample.getContainerType());
            response.setPreservative(sample.getPreservative());
            response.setRejected(sample.getRejected());
            response.setRejectionReason(sample.getRejectionReason());
        } else {
            // No sample collected yet - status should be null to indicate pending collection
            response.setStatus(null); // No status until sample is collected
        }
        
        return response;
    }
    
    /**
     * Validate status transition
     */
    private boolean isValidStatusTransition(SampleStatus currentStatus, SampleStatus newStatus) {
        if (currentStatus == null) return true;
        
        SampleStatus[] validNextStatuses = currentStatus.getNextPossibleStatuses();
        for (SampleStatus validStatus : validNextStatuses) {
            if (validStatus == newStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate unique sample number
     */
    private String generateSampleNumber() {
        // Generate sample number in format: S-YYYYMMDD-HHMMSS-XXX
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String randomSuffix = String.format("%03d", (int)(Math.random() * 1000));
        return "S-" + timestamp + "-" + randomSuffix;
    }
}
