package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.*;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.LabTestRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import com.sivalab.laboperations.repository.VisitRepository;
import com.sivalab.laboperations.validator.TestResultsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LabTestService {
    
    private final LabTestRepository labTestRepository;
    private final VisitRepository visitRepository;
    private final TestTemplateRepository testTemplateRepository;
    private final TestTemplateService testTemplateService;
    private final TestResultsValidator testResultsValidator;

    @Autowired
    public LabTestService(LabTestRepository labTestRepository,
                         VisitRepository visitRepository,
                         TestTemplateRepository testTemplateRepository,
                         TestTemplateService testTemplateService,
                         TestResultsValidator testResultsValidator) {
        this.labTestRepository = labTestRepository;
        this.visitRepository = visitRepository;
        this.testTemplateRepository = testTemplateRepository;
        this.testTemplateService = testTemplateService;
        this.testResultsValidator = testResultsValidator;
    }
    
    /**
     * Add test to visit
     */
    public LabTestResponse addTestToVisit(Long visitId, AddTestToVisitRequest request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));
        
        TestTemplate testTemplate = testTemplateRepository.findById(request.getTestTemplateId())
                .orElseThrow(() -> new RuntimeException("Test template not found with ID: " + request.getTestTemplateId()));
        
        // Use provided price or default to base price
        BigDecimal price = request.getPrice() != null ? request.getPrice() : testTemplate.getBasePrice();
        
        LabTest labTest = new LabTest(visit, testTemplate, price);
        labTest = labTestRepository.save(labTest);
        
        return convertToResponse(labTest);
    }
    
    /**
     * Get lab test by ID
     */
    @Transactional(readOnly = true)
    public LabTestResponse getLabTest(Long testId) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found with ID: " + testId));
        return convertToResponse(labTest);
    }
    
    /**
     * Get lab tests for visit
     */
    @Transactional(readOnly = true)
    public List<LabTestResponse> getLabTestsForVisit(Long visitId) {
        return labTestRepository.findByVisitVisitId(visitId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update test results with validation
     */
    public LabTestResponse updateTestResults(Long visitId, Long testId, UpdateTestResultsRequest request) {
        LabTest labTest = labTestRepository.findByVisitVisitIdAndTestId(visitId, testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found with ID: " + testId + " for visit: " + visitId));

        // Validate results against test template parameters
        TestTemplate testTemplate = labTest.getTestTemplate();
        if (testTemplate != null && testTemplate.getParameters() != null) {
            testResultsValidator.validateResults(request.getResults(), testTemplate.getParameters());
            // Also validate NABL compliance
            testResultsValidator.validateNABLCompliance(request.getResults());
        }

        labTest.setResults(request.getResults());
        labTest.setResultsEnteredAt(LocalDateTime.now());
        labTest.setStatus(TestStatus.COMPLETED);
        labTest = labTestRepository.save(labTest);

        return convertToResponse(labTest);
    }
    
    /**
     * Approve test results
     */
    public LabTestResponse approveTest(Long visitId, Long testId, ApproveTestRequest request) {
        LabTest labTest = labTestRepository.findByVisitVisitIdAndTestId(visitId, testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found with ID: " + testId + " for visit: " + visitId));
        
        if (labTest.getStatus() != TestStatus.COMPLETED) {
            throw new RuntimeException("Cannot approve test that is not completed");
        }
        
        labTest.setApproved(true);
        labTest.setApprovedBy(request.getApprovedBy());
        labTest.setApprovedAt(LocalDateTime.now());
        labTest.setStatus(TestStatus.APPROVED);
        
        labTest = labTestRepository.save(labTest);

        // Check if all tests for the visit are completed and approved
        long incompleteTests = labTestRepository.countIncompleteTestsForVisit(visitId);
        long pendingApprovalTests = labTestRepository.countPendingTestsForVisit(visitId);

        if (incompleteTests == 0 && pendingApprovalTests == 0) {
            // All tests are completed and approved - update visit status to approved
            Visit visit = labTest.getVisit();
            visit.setStatus(VisitStatus.APPROVED);
            visitRepository.save(visit);
        }

        return convertToResponse(labTest);
    }
    
    /**
     * Get tests needing approval
     */
    @Transactional(readOnly = true)
    public List<LabTestResponse> getTestsNeedingApproval() {
        return labTestRepository.findTestsNeedingApproval().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete lab test
     */
    public void deleteLabTest(Long visitId, Long testId) {
        LabTest labTest = labTestRepository.findByVisitVisitIdAndTestId(visitId, testId)
                .orElseThrow(() -> new RuntimeException("Lab test not found with ID: " + testId + " for visit: " + visitId));
        
        if (labTest.getApproved()) {
            throw new RuntimeException("Cannot delete approved lab test");
        }
        
        labTestRepository.delete(labTest);
    }
    
    /**
     * Convert LabTest entity to LabTestResponse DTO
     */
    public LabTestResponse convertToResponse(LabTest labTest) {
        LabTestResponse response = new LabTestResponse();
        response.setTestId(labTest.getTestId());
        response.setVisitId(labTest.getVisit().getVisitId());
        response.setTestTemplate(testTemplateService.convertToResponse(labTest.getTestTemplate()));
        response.setStatus(labTest.getStatus());
        response.setPrice(labTest.getPrice());
        response.setResults(labTest.getResults());
        response.setApproved(labTest.getApproved());
        response.setApprovedBy(labTest.getApprovedBy());
        response.setApprovedAt(labTest.getApprovedAt());
        return response;
    }

    /**
     * Get all lab tests
     */
    public List<LabTest> getAllLabTests() {
        return labTestRepository.findAll();
    }

    /**
     * Get lab test by ID
     */
    public Optional<LabTest> getLabTestById(Long testId) {
        return labTestRepository.findById(testId);
    }

    /**
     * Get lab tests by visit ID
     */
    public List<LabTest> getLabTestsByVisitId(Long visitId) {
        return labTestRepository.findByVisitVisitId(visitId);
    }

    /**
     * Get lab tests by status
     */
    public List<LabTest> getLabTestsByStatus(String status) {
        return labTestRepository.findByStatus(TestStatus.valueOf(status.toUpperCase()));
    }

    /**
     * Get pending lab tests
     */
    public List<LabTest> getPendingLabTests() {
        return labTestRepository.findByStatus(TestStatus.PENDING);
    }

    /**
     * Get completed lab tests
     */
    public List<LabTest> getCompletedLabTests() {
        return labTestRepository.findByStatus(TestStatus.COMPLETED);
    }

    /**
     * Get lab test statistics
     */
    public Map<String, Object> getLabTestStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalTests = labTestRepository.count();
        long pendingTests = labTestRepository.countByStatus(TestStatus.PENDING);
        long completedTests = labTestRepository.countByStatus(TestStatus.COMPLETED);
        long approvedTests = labTestRepository.countByApprovedTrue();

        stats.put("totalTests", totalTests);
        stats.put("pendingTests", pendingTests);
        stats.put("completedTests", completedTests);
        stats.put("approvedTests", approvedTests);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }
}
