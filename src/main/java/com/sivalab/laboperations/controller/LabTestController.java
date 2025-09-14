package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.LabTest;
import com.sivalab.laboperations.service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Lab Test Management
 * Provides endpoints for lab test operations and history tracking
 */
@RestController
@RequestMapping("/lab-tests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LabTestController {
    
    private final LabTestService labTestService;
    
    @Autowired
    public LabTestController(LabTestService labTestService) {
        this.labTestService = labTestService;
    }
    
    /**
     * Get all lab tests
     * GET /lab-tests
     */
    @GetMapping
    public ResponseEntity<List<LabTest>> getAllLabTests() {
        try {
            List<LabTest> labTests = labTestService.getAllLabTests();
            return ResponseEntity.ok(labTests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab tests: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get lab test by ID
     * GET /lab-tests/{testId}
     */
    @GetMapping("/{testId}")
    public ResponseEntity<LabTest> getLabTestById(@PathVariable Long testId) {
        try {
            Optional<LabTest> labTest = labTestService.getLabTestById(testId);
            return labTest.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab test: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get lab tests by visit ID
     * GET /lab-tests/visit/{visitId}
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<LabTest>> getLabTestsByVisitId(@PathVariable Long visitId) {
        try {
            List<LabTest> labTests = labTestService.getLabTestsByVisitId(visitId);
            return ResponseEntity.ok(labTests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab tests for visit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get lab tests by status
     * GET /lab-tests/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabTest>> getLabTestsByStatus(@PathVariable String status) {
        try {
            List<LabTest> labTests = labTestService.getLabTestsByStatus(status);
            return ResponseEntity.ok(labTests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab tests by status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get pending lab tests (for technician dashboard)
     * GET /lab-tests/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<LabTest>> getPendingLabTests() {
        try {
            List<LabTest> pendingTests = labTestService.getPendingLabTests();
            return ResponseEntity.ok(pendingTests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve pending lab tests: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get completed lab tests
     * GET /lab-tests/completed
     */
    @GetMapping("/completed")
    public ResponseEntity<List<LabTest>> getCompletedLabTests() {
        try {
            List<LabTest> completedTests = labTestService.getCompletedLabTests();
            return ResponseEntity.ok(completedTests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve completed lab tests: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get lab test statistics
     * GET /lab-tests/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Object> getLabTestStatistics() {
        try {
            Object statistics = labTestService.getLabTestStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab test statistics: " + e.getMessage(), e);
        }
    }
}
