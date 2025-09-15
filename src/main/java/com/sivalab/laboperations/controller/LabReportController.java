package com.sivalab.laboperations.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.LabReport;
import com.sivalab.laboperations.entity.ReportStatus;
import com.sivalab.laboperations.entity.ReportType;
import com.sivalab.laboperations.service.LabReportService;
import com.sivalab.laboperations.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for NABL-compliant laboratory reports with ULR numbers
 */
@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class LabReportController {

    private final LabReportService labReportService;
    private final PdfReportService pdfReportService;

    @Autowired
    public LabReportController(LabReportService labReportService, PdfReportService pdfReportService) {
        this.labReportService = labReportService;
        this.pdfReportService = pdfReportService;
    }
    
    /**
     * Create a new lab report for a visit
     * POST /reports
     */
    @PostMapping
    public ResponseEntity<LabReport> createReport(@RequestBody CreateReportRequest request) {
        try {
            LabReport report = labReportService.createReport(request.getVisitId(), request.getReportType());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate report content
     * POST /reports/{reportId}/generate
     */
    @PostMapping("/{reportId}/generate")
    public ResponseEntity<LabReport> generateReport(@PathVariable Long reportId, 
                                                   @RequestBody GenerateReportRequest request) {
        try {
            LabReport report = labReportService.generateReport(reportId, request.getReportData(), 
                                                             request.getTemplateVersion());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Authorize a report
     * POST /reports/{reportId}/authorize
     */
    @PostMapping("/{reportId}/authorize")
    public ResponseEntity<LabReport> authorizeReport(@PathVariable Long reportId, 
                                                    @RequestBody AuthorizeReportRequest request) {
        try {
            LabReport report = labReportService.authorizeReport(reportId, request.getAuthorizedBy());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to authorize report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark report as sent
     * POST /reports/{reportId}/send
     */
    @PostMapping("/{reportId}/send")
    public ResponseEntity<LabReport> markReportAsSent(@PathVariable Long reportId) {
        try {
            LabReport report = labReportService.markReportAsSent(reportId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark report as sent: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get report by ULR number
     * GET /reports/ulr/{ulrNumber}
     */
    @GetMapping("/ulr/{ulrNumber}")
    public ResponseEntity<LabReport> getReportByUlrNumber(@PathVariable String ulrNumber) {
        Optional<LabReport> report = labReportService.getReportByUlrNumber(ulrNumber);
        return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all reports for a visit
     * GET /reports/visit/{visitId}
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<LabReport>> getReportsForVisit(@PathVariable Long visitId) {
        List<LabReport> reports = labReportService.getReportsForVisit(visitId);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get latest report for a visit
     * GET /reports/visit/{visitId}/latest
     */
    @GetMapping("/visit/{visitId}/latest")
    public ResponseEntity<LabReport> getLatestReportForVisit(@PathVariable Long visitId) {
        Optional<LabReport> report = labReportService.getLatestReportForVisit(visitId);
        return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get reports by status
     * GET /reports/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LabReport>> getReportsByStatus(@PathVariable ReportStatus status) {
        List<LabReport> reports = labReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports pending authorization
     * GET /reports/pending-authorization
     */
    @GetMapping("/pending-authorization")
    public ResponseEntity<List<LabReport>> getReportsPendingAuthorization() {
        List<LabReport> reports = labReportService.getReportsPendingAuthorization();
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports generated within date range
     * GET /reports/generated-between
     */
    @GetMapping("/generated-between")
    public ResponseEntity<List<LabReport>> getReportsGeneratedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LabReport> reports = labReportService.getReportsGeneratedBetween(startDate, endDate);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get reports authorized by specific person
     * GET /reports/authorized-by/{authorizedBy}
     */
    @GetMapping("/authorized-by/{authorizedBy}")
    public ResponseEntity<List<LabReport>> getReportsAuthorizedBy(@PathVariable String authorizedBy) {
        List<LabReport> reports = labReportService.getReportsAuthorizedBy(authorizedBy);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Create amended report
     * POST /reports/{reportId}/amend
     */
    @PostMapping("/{reportId}/amend")
    public ResponseEntity<LabReport> createAmendedReport(@PathVariable Long reportId, 
                                                        @RequestBody AmendReportRequest request) {
        try {
            LabReport report = labReportService.createAmendedReport(reportId, request.getReason());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create amended report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create supplementary report
     * POST /reports/{reportId}/supplement
     */
    @PostMapping("/{reportId}/supplement")
    public ResponseEntity<LabReport> createSupplementaryReport(@PathVariable Long reportId, 
                                                              @RequestBody SupplementReportRequest request) {
        try {
            LabReport report = labReportService.createSupplementaryReport(reportId, request.getAdditionalData());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create supplementary report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get individual report by ID
     * GET /reports/{reportId}
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<LabReport> getReportById(@PathVariable Long reportId) {
        Optional<LabReport> report = labReportService.getReportById(reportId);
        return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get report statistics
     * GET /reports/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<LabReportService.ReportStatistics> getReportStatistics() {
        LabReportService.ReportStatistics stats = labReportService.getReportStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Generate PDF report
     * GET /reports/{reportId}/pdf
     */
    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<byte[]> generatePdfReport(@PathVariable Long reportId) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            byte[] pdfBytes = pdfReportService.generatePdfReport(report);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report_" + report.getUlrNumber().replace("/", "_") + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate HTML-based PDF report
     * GET /reports/{reportId}/pdf-html
     */
    @GetMapping("/{reportId}/pdf-html")
    public ResponseEntity<byte[]> generateHtmlPdfReport(@PathVariable Long reportId) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            byte[] pdfBytes = pdfReportService.generatePdfFromHtml(report);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report_html_" + report.getUlrNumber().replace("/", "_") + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Preview report as HTML (for testing)
     * GET /reports/{reportId}/preview
     */
    @GetMapping("/{reportId}/preview")
    public ResponseEntity<String> previewReportHtml(@PathVariable Long reportId) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            // Generate HTML content for preview
            String htmlContent = generatePreviewHtml(report);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper method to generate HTML preview
     */
    private String generatePreviewHtml(LabReport report) throws IOException {
        // This is a simplified version for preview
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:20px;}</style>");
        html.append("</head><body>");
        html.append("<h1>SLN CITY LABORATORY</h1>");
        html.append("<p><strong>ULR Number:</strong> ").append(report.getUlrNumber()).append("</p>");
        html.append("<p><strong>Report Type:</strong> ").append(report.getReportType()).append("</p>");
        html.append("<p><strong>Status:</strong> ").append(report.getReportStatus()).append("</p>");

        if (report.getVisit() != null) {
            html.append("<h2>Patient Information</h2>");
            html.append("<p>Visit ID: ").append(report.getVisit().getVisitId()).append("</p>");
            html.append("<p>Visit Date: ").append(report.getVisit().getCreatedAt()).append("</p>");
        }

        if (report.getReportData() != null) {
            html.append("<h2>Report Data</h2>");
            html.append("<pre>").append(report.getReportData()).append("</pre>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    // Request DTOs
    public static class CreateReportRequest {
        private Long visitId;
        private ReportType reportType = ReportType.STANDARD;
        
        public Long getVisitId() { return visitId; }
        public void setVisitId(Long visitId) { this.visitId = visitId; }
        public ReportType getReportType() { return reportType; }
        public void setReportType(ReportType reportType) { this.reportType = reportType; }
    }
    
    public static class GenerateReportRequest {
        private JsonNode reportData;
        private String templateVersion;
        
        public JsonNode getReportData() { return reportData; }
        public void setReportData(JsonNode reportData) { this.reportData = reportData; }
        public String getTemplateVersion() { return templateVersion; }
        public void setTemplateVersion(String templateVersion) { this.templateVersion = templateVersion; }
    }
    
    public static class AuthorizeReportRequest {
        private String authorizedBy;
        
        public String getAuthorizedBy() { return authorizedBy; }
        public void setAuthorizedBy(String authorizedBy) { this.authorizedBy = authorizedBy; }
    }
    
    public static class AmendReportRequest {
        private String reason;
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class SupplementReportRequest {
        private JsonNode additionalData;
        
        public JsonNode getAdditionalData() { return additionalData; }
        public void setAdditionalData(JsonNode additionalData) { this.additionalData = additionalData; }
    }
}
