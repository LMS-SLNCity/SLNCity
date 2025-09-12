package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.*;
import com.sivalab.laboperations.entity.VisitStatus;
import com.sivalab.laboperations.service.LabTestService;
import com.sivalab.laboperations.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visits")
public class VisitController {
    
    private final VisitService visitService;
    private final LabTestService labTestService;
    
    @Autowired
    public VisitController(VisitService visitService, LabTestService labTestService) {
        this.visitService = visitService;
        this.labTestService = labTestService;
    }
    
    /**
     * Create a new visit
     * POST /visits
     */
    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody CreateVisitRequest request) {
        try {
            // Additional validation for null patient details
            if (request.getPatientDetails() == null || request.getPatientDetails().isNull()) {
                throw new IllegalArgumentException("Patient details cannot be null");
            }

            VisitResponse response = visitService.createVisit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create visit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get visit by ID
     * GET /visits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getVisit(@PathVariable Long id) {
        try {
            VisitResponse response = visitService.getVisit(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all visits or filter by status
     * GET /visits?status=pending
     */
    @GetMapping
    public ResponseEntity<List<VisitResponse>> getVisits(@RequestParam(required = false) String status) {
        try {
            List<VisitResponse> visits;
            if (status != null) {
                // If status parameter is provided but empty or whitespace, return error
                if (status.isEmpty() || status.trim().isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                VisitStatus visitStatus = VisitStatus.fromValue(status);
                visits = visitService.getVisitsByStatus(visitStatus);
            } else {
                visits = visitService.getAllVisits();
            }
            return ResponseEntity.ok(visits);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search visits by patient phone
     * GET /visits/search?phone=9999999999
     */
    @GetMapping("/search")
    public ResponseEntity<List<VisitResponse>> searchVisits(@RequestParam(required = false) String phone) {
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<VisitResponse> visits = visitService.getVisitsByPatientPhone(phone);
            return ResponseEntity.ok(visits);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search visits: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update visit status
     * PATCH /visits/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<VisitResponse> updateVisitStatus(@PathVariable Long id,
                                                          @RequestParam String status) {
        try {
            // Validate status parameter
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            VisitStatus newStatus = VisitStatus.fromValue(status);
            VisitResponse response = visitService.updateVisitStatus(id, newStatus);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to update visit status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete visit
     * DELETE /visits/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        try {
            visitService.deleteVisit(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to delete visit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add test to visit
     * POST /visits/{visitId}/tests
     */
    @PostMapping("/{visitId}/tests")
    public ResponseEntity<LabTestResponse> addTestToVisit(@PathVariable Long visitId,
                                                         @Valid @RequestBody AddTestToVisitRequest request) {
        try {
            LabTestResponse response = labTestService.addTestToVisit(visitId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to add test to visit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get tests for visit
     * GET /visits/{visitId}/tests
     */
    @GetMapping("/{visitId}/tests")
    public ResponseEntity<List<LabTestResponse>> getTestsForVisit(@PathVariable Long visitId) {
        try {
            List<LabTestResponse> tests = labTestService.getLabTestsForVisit(visitId);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tests for visit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update test results
     * PATCH /visits/{visitId}/tests/{testId}/results
     */
    @PatchMapping("/{visitId}/tests/{testId}/results")
    public ResponseEntity<LabTestResponse> updateTestResults(@PathVariable Long visitId,
                                                            @PathVariable Long testId,
                                                            @Valid @RequestBody UpdateTestResultsRequest request) {
        try {
            // Additional validation for null results
            if (request.getResults() == null || request.getResults().isNull()) {
                throw new IllegalArgumentException("Results cannot be null");
            }

            LabTestResponse response = labTestService.updateTestResults(visitId, testId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to update test results: " + e.getMessage(), e);
        }
    }
    
    /**
     * Approve test results
     * PATCH /visits/{visitId}/tests/{testId}/approve
     */
    @PatchMapping("/{visitId}/tests/{testId}/approve")
    public ResponseEntity<LabTestResponse> approveTest(@PathVariable Long visitId,
                                                      @PathVariable Long testId,
                                                      @Valid @RequestBody ApproveTestRequest request) {
        try {
            LabTestResponse response = labTestService.approveTest(visitId, testId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to approve test: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete test from visit
     * DELETE /visits/{visitId}/tests/{testId}
     */
    @DeleteMapping("/{visitId}/tests/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long visitId, @PathVariable Long testId) {
        try {
            labTestService.deleteLabTest(visitId, testId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to delete test: " + e.getMessage(), e);
        }
    }
}
