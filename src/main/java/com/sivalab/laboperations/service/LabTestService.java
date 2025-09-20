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
import java.util.stream.Collectors;

@Service
@Transactional
public class LabTestService {
    
    private final LabTestRepository labTestRepository;
    private final VisitRepository visitRepository;
    private final TestTemplateRepository testTemplateRepository;
    private final TestTemplateService testTemplateService;
    private final TestResultsValidator testResultsValidator;
    private final TATService tatService;

    @Autowired
    public LabTestService(LabTestRepository labTestRepository,
                         VisitRepository visitRepository,
                         TestTemplateRepository testTemplateRepository,
                         TestTemplateService testTemplateService,
                         TestResultsValidator testResultsValidator,
                         TATService tatService) {
        this.labTestRepository = labTestRepository;
        this.visitRepository = visitRepository;
        this.testTemplateRepository = testTemplateRepository;
        this.testTemplateService = testTemplateService;
        this.testResultsValidator = testResultsValidator;
        this.tatService = tatService;
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

        tatService.getTATByTestTemplate_TemplateId(testTemplate.getTemplateId()).ifPresent(tat -> {
            LocalDateTime now = LocalDateTime.now();
            final LocalDateTime expectedCompletionTime;
            if (tat.getTatUnit() == TAT.TATUnit.HOURS) {
                expectedCompletionTime = now.plusHours(tat.getTatValue());
            } else if (tat.getTatUnit() == TAT.TATUnit.DAYS) {
                expectedCompletionTime = now.plusDays(tat.getTatValue());
            } else {
                expectedCompletionTime = now;
            }
            labTest.setExpectedCompletionTime(expectedCompletionTime);
        });

        LabTest savedLabTest = labTestRepository.save(labTest);
        
        return convertToResponse(savedLabTest);
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
        response.setExpectedCompletionTime(labTest.getExpectedCompletionTime());
        return response;
    }
}
