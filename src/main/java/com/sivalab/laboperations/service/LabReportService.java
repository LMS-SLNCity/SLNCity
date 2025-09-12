package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.LabReportRepository;
import com.sivalab.laboperations.repository.VisitRepository;
import com.sivalab.laboperations.repository.LabTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing NABL-compliant laboratory reports with ULR numbers
 */
@Service
@Transactional
public class LabReportService {
    
    private final LabReportRepository labReportRepository;
    private final VisitRepository visitRepository;
    private final LabTestRepository labTestRepository;
    private final UlrService ulrService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LabReportService(LabReportRepository labReportRepository,
                           VisitRepository visitRepository,
                           LabTestRepository labTestRepository,
                           UlrService ulrService,
                           ObjectMapper objectMapper) {
        this.labReportRepository = labReportRepository;
        this.visitRepository = visitRepository;
        this.labTestRepository = labTestRepository;
        this.ulrService = ulrService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a new lab report for a visit
     */
    public LabReport createReport(Long visitId, ReportType reportType) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));

        // Get all lab tests for this visit
        List<LabTest> labTests = labTestRepository.findByVisitVisitId(visitId);

        LabReport report = new LabReport(visit, reportType);

        // Generate ULR number using ULR service
        String ulrNumber = ulrService.generateUlrNumber();
        report.setUlrNumber(ulrNumber);

        // Generate comprehensive report data with test results
        JsonNode reportData = generateReportData(visit, labTests);
        report.setReportData(reportData);

        return labReportRepository.save(report);
    }

    /**
     * Generate comprehensive report data including all test results
     */
    private JsonNode generateReportData(Visit visit, List<LabTest> labTests) {
        ObjectNode reportData = objectMapper.createObjectNode();

        // Add patient information
        ObjectNode patientInfo = objectMapper.createObjectNode();
        JsonNode patientDetails = visit.getPatientDetails();
        patientInfo.set("details", patientDetails);
        patientInfo.put("visitId", visit.getVisitId());
        patientInfo.put("visitDate", visit.getCreatedAt().toString());
        patientInfo.put("status", visit.getStatus().toString());
        reportData.set("patient", patientInfo);

        // Add test results
        ArrayNode testsArray = objectMapper.createArrayNode();

        for (LabTest labTest : labTests) {
            ObjectNode testNode = objectMapper.createObjectNode();
            testNode.put("testId", labTest.getTestId());
            testNode.put("testName", labTest.getTestTemplate().getName());
            testNode.put("description", labTest.getTestTemplate().getDescription());
            testNode.put("status", labTest.getStatus().toString());
            testNode.put("price", labTest.getPrice());
            testNode.put("approved", labTest.getApproved() != null && labTest.getApproved());

            if (labTest.getApprovedBy() != null) {
                testNode.put("approvedBy", labTest.getApprovedBy());
            }

            if (labTest.getApprovedAt() != null) {
                testNode.put("approvedAt", labTest.getApprovedAt().toString());
            }

            // Add test parameters from template
            testNode.set("parameters", labTest.getTestTemplate().getParameters());

            // Add test results if available
            if (labTest.getResults() != null) {
                testNode.set("results", labTest.getResults());
            }

            testsArray.add(testNode);
        }

        reportData.set("tests", testsArray);

        // Add summary information
        ObjectNode summary = objectMapper.createObjectNode();
        summary.put("totalTests", labTests.size());
        summary.put("approvedTests", (int) labTests.stream().filter(test -> test.getApproved() != null && test.getApproved()).count());
        summary.put("pendingTests", (int) labTests.stream().filter(test -> test.getApproved() == null || !test.getApproved()).count());
        summary.put("totalAmount", labTests.stream().mapToDouble(test -> test.getPrice().doubleValue()).sum());
        reportData.set("summary", summary);

        return reportData;
    }

    /**
     * Generate report content and mark as generated
     */
    public LabReport generateReport(Long reportId, JsonNode reportData, String templateVersion) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        report.setReportData(reportData);
        report.markAsGenerated(templateVersion);
        
        return labReportRepository.save(report);
    }
    
    /**
     * Authorize a report
     */
    public LabReport authorizeReport(Long reportId, String authorizedBy) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        if (report.getReportStatus() != ReportStatus.GENERATED) {
            throw new RuntimeException("Report must be generated before authorization");
        }
        
        report.authorize(authorizedBy);
        return labReportRepository.save(report);
    }
    
    /**
     * Mark report as sent
     */
    public LabReport markReportAsSent(Long reportId) {
        LabReport report = labReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        if (report.getReportStatus() != ReportStatus.AUTHORIZED) {
            throw new RuntimeException("Report must be authorized before sending");
        }
        
        report.markAsSent();
        return labReportRepository.save(report);
    }
    
    /**
     * Get report by ID
     */
    @Transactional(readOnly = true)
    public Optional<LabReport> getReportById(Long reportId) {
        return labReportRepository.findById(reportId);
    }

    /**
     * Get report by ULR number
     */
    @Transactional(readOnly = true)
    public Optional<LabReport> getReportByUlrNumber(String ulrNumber) {
        return labReportRepository.findByUlrNumber(ulrNumber);
    }
    
    /**
     * Get all reports for a visit
     */
    @Transactional(readOnly = true)
    public List<LabReport> getReportsForVisit(Long visitId) {
        return labReportRepository.findByVisitVisitId(visitId);
    }
    
    /**
     * Get latest report for a visit
     */
    @Transactional(readOnly = true)
    public Optional<LabReport> getLatestReportForVisit(Long visitId) {
        List<LabReport> reports = labReportRepository.findLatestReportForVisit(visitId);
        return reports.isEmpty() ? Optional.empty() : Optional.of(reports.get(0));
    }
    
    /**
     * Get reports by status
     */
    @Transactional(readOnly = true)
    public List<LabReport> getReportsByStatus(ReportStatus status) {
        return labReportRepository.findByReportStatus(status);
    }
    
    /**
     * Get reports pending authorization
     */
    @Transactional(readOnly = true)
    public List<LabReport> getReportsPendingAuthorization() {
        return labReportRepository.findPendingAuthorization();
    }
    
    /**
     * Get reports generated within date range
     */
    @Transactional(readOnly = true)
    public List<LabReport> getReportsGeneratedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return labReportRepository.findByGeneratedAtBetween(startDate, endDate);
    }
    
    /**
     * Get reports authorized by specific person
     */
    @Transactional(readOnly = true)
    public List<LabReport> getReportsAuthorizedBy(String authorizedBy) {
        return labReportRepository.findByAuthorizedBy(authorizedBy);
    }
    
    /**
     * Create amended report
     */
    public LabReport createAmendedReport(Long originalReportId, String reason) {
        LabReport originalReport = labReportRepository.findById(originalReportId)
                .orElseThrow(() -> new RuntimeException("Original report not found with ID: " + originalReportId));
        
        // Create amended report
        LabReport amendedReport = new LabReport(originalReport.getVisit(), ReportType.AMENDED);
        
        // Copy original report data
        amendedReport.setReportData(originalReport.getReportData());
        amendedReport.setTemplateVersion(originalReport.getTemplateVersion());
        
        return labReportRepository.save(amendedReport);
    }
    
    /**
     * Create supplementary report
     */
    public LabReport createSupplementaryReport(Long originalReportId, JsonNode additionalData) {
        LabReport originalReport = labReportRepository.findById(originalReportId)
                .orElseThrow(() -> new RuntimeException("Original report not found with ID: " + originalReportId));
        
        // Create supplementary report
        LabReport supplementaryReport = new LabReport(originalReport.getVisit(), ReportType.SUPPLEMENTARY);
        supplementaryReport.setReportData(additionalData);
        
        return labReportRepository.save(supplementaryReport);
    }
    
    /**
     * Get report statistics
     */
    @Transactional(readOnly = true)
    public ReportStatistics getReportStatistics() {
        long totalReports = labReportRepository.count();
        long draftReports = labReportRepository.countByReportStatus(ReportStatus.DRAFT);
        long generatedReports = labReportRepository.countByReportStatus(ReportStatus.GENERATED);
        long authorizedReports = labReportRepository.countByReportStatus(ReportStatus.AUTHORIZED);
        long sentReports = labReportRepository.countByReportStatus(ReportStatus.SENT);
        long todayReports = labReportRepository.countReportsGeneratedToday();
        
        return new ReportStatistics(totalReports, draftReports, generatedReports, 
                                  authorizedReports, sentReports, todayReports);
    }
    
    /**
     * Validate report data for NABL compliance
     */
    public boolean validateNablCompliance(LabReport report) {
        // Basic NABL compliance checks
        if (report.getUlrNumber() == null || report.getUlrNumber().trim().isEmpty()) {
            return false;
        }
        
        if (!ulrService.isValidUlrFormat(report.getUlrNumber())) {
            return false;
        }
        
        if (report.getVisit() == null) {
            return false;
        }
        
        // Additional NABL compliance checks can be added here
        return true;
    }
    
    /**
     * Inner class for report statistics
     */
    public static class ReportStatistics {
        private final long totalReports;
        private final long draftReports;
        private final long generatedReports;
        private final long authorizedReports;
        private final long sentReports;
        private final long todayReports;
        
        public ReportStatistics(long totalReports, long draftReports, long generatedReports,
                              long authorizedReports, long sentReports, long todayReports) {
            this.totalReports = totalReports;
            this.draftReports = draftReports;
            this.generatedReports = generatedReports;
            this.authorizedReports = authorizedReports;
            this.sentReports = sentReports;
            this.todayReports = todayReports;
        }
        
        // Getters
        public long getTotalReports() { return totalReports; }
        public long getDraftReports() { return draftReports; }
        public long getGeneratedReports() { return generatedReports; }
        public long getAuthorizedReports() { return authorizedReports; }
        public long getSentReports() { return sentReports; }
        public long getTodayReports() { return todayReports; }
    }
}
