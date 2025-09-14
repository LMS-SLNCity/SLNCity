package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.SampleCollectionRequest;
import com.sivalab.laboperations.dto.SampleResponse;
import com.sivalab.laboperations.service.SampleCollectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sample-collection")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SampleCollectionController {
    
    private final SampleCollectionService sampleCollectionService;
    
    @Autowired
    public SampleCollectionController(SampleCollectionService sampleCollectionService) {
        this.sampleCollectionService = sampleCollectionService;
    }
    
    /**
     * Get all tests pending sample collection
     * GET /sample-collection/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<SampleResponse>> getPendingSamples() {
        try {
            List<SampleResponse> samples = sampleCollectionService.getPendingSamples();
            return ResponseEntity.ok(samples);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get pending samples: " + e.getMessage(), e);
        }
    }
    
    /**
     * Collect sample for a test
     * POST /sample-collection/collect/{testId}
     */
    @PostMapping("/collect/{testId}")
    public ResponseEntity<SampleResponse> collectSample(@PathVariable Long testId,
                                                       @Valid @RequestBody SampleCollectionRequest request) {
        try {
            SampleResponse response = sampleCollectionService.collectSample(testId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("already collected")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw new RuntimeException("Failed to collect sample: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update sample status
     * PUT /sample-collection/{sampleId}/status
     */
    @PutMapping("/{sampleId}/status")
    public ResponseEntity<SampleResponse> updateSampleStatus(@PathVariable Long sampleId,
                                                            @RequestParam String status) {
        try {
            SampleResponse response = sampleCollectionService.updateSampleStatus(sampleId, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Invalid status")) {
                return ResponseEntity.badRequest().build();
            }
            throw new RuntimeException("Failed to update sample status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get sample by ID
     * GET /sample-collection/{sampleId}
     */
    @GetMapping("/{sampleId}")
    public ResponseEntity<SampleResponse> getSample(@PathVariable Long sampleId) {
        try {
            SampleResponse response = sampleCollectionService.getSample(sampleId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to get sample: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all samples for a visit
     * GET /sample-collection/visit/{visitId}
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<SampleResponse>> getSamplesByVisit(@PathVariable Long visitId) {
        try {
            List<SampleResponse> samples = sampleCollectionService.getSamplesByVisit(visitId);
            return ResponseEntity.ok(samples);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get samples for visit: " + e.getMessage(), e);
        }
    }
}
