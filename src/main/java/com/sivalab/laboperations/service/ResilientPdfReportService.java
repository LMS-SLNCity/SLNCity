package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.LabReport;
import com.sivalab.laboperations.entity.ReportStatus;
import com.sivalab.laboperations.entity.ReportType;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Resilient PDF Report Service with Fault Tolerance Patterns
 * Wraps the original PdfReportService with circuit breaker, retry, rate limiting, etc.
 */
@Service
public class ResilientPdfReportService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientPdfReportService.class);

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * Generate PDF report with fault tolerance
     */
    @CircuitBreaker(name = "pdfGeneration", fallbackMethod = "fallbackGeneratePdf")
    @Retry(name = "externalService")
    @RateLimiter(name = "pdf")
    @Bulkhead(name = "pdf")
    @TimeLimiter(name = "pdf")
    public CompletableFuture<byte[]> generatePdfReportResilient(LabReport labReport) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating PDF report for ULR: {}", labReport.getUlrNumber());
                long startTime = System.currentTimeMillis();
                
                byte[] result = pdfReportService.generatePdfReport(labReport);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Successfully generated PDF report for ULR: {} in {}ms, size: {} bytes", 
                    labReport.getUlrNumber(), duration, result.length);
                
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate PDF report for ULR: {}", labReport.getUlrNumber(), e);
                throw new RuntimeException("PDF report generation failed", e);
            }
        });
    }

    /**
     * Generate HTML report with fault tolerance (fallback for PDF)
     */
    @CircuitBreaker(name = "pdfGeneration", fallbackMethod = "fallbackGenerateHtml")
    @Retry(name = "externalService")
    @RateLimiter(name = "pdf")
    @Bulkhead(name = "pdf")
    @TimeLimiter(name = "pdf")
    public CompletableFuture<String> generateHtmlReportResilient(LabReport labReport) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating HTML report for ULR: {}", labReport.getUlrNumber());
                long startTime = System.currentTimeMillis();
                
                String result = pdfReportService.generateHtmlReport(labReport);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Successfully generated HTML report for ULR: {} in {}ms, size: {} chars", 
                    labReport.getUlrNumber(), duration, result.length());
                
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate HTML report for ULR: {}", labReport.getUlrNumber(), e);
                throw new RuntimeException("HTML report generation failed", e);
            }
        });
    }

    /**
     * Generate report with embedded barcodes with fault tolerance
     */
    @CircuitBreaker(name = "pdfGeneration", fallbackMethod = "fallbackGeneratePdfWithBarcodes")
    @Retry(name = "externalService")
    @RateLimiter(name = "pdf")
    @Bulkhead(name = "pdf")
    @TimeLimiter(name = "pdf")
    public CompletableFuture<byte[]> generatePdfReportWithBarcodesResilient(LabReport labReport) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating PDF report with barcodes for ULR: {}", labReport.getUlrNumber());
                long startTime = System.currentTimeMillis();
                
                // Try to generate PDF with barcodes first
                byte[] result = pdfReportService.generatePdfReport(labReport);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Successfully generated PDF report with barcodes for ULR: {} in {}ms, size: {} bytes", 
                    labReport.getUlrNumber(), duration, result.length);
                
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate PDF report with barcodes for ULR: {}", labReport.getUlrNumber(), e);
                throw new RuntimeException("PDF report with barcodes generation failed", e);
            }
        });
    }

    // Fallback Methods

    /**
     * Fallback method for PDF generation failures
     * Returns a simple text-based PDF
     */
    public CompletableFuture<byte[]> fallbackGeneratePdf(LabReport labReport, Exception ex) {
        logger.warn("PDF generation fallback triggered for ULR: {} due to: {}", 
            labReport.getUlrNumber(), ex.getMessage());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generateSimpleFallbackPdf(labReport);
            } catch (Exception e) {
                logger.error("Even fallback PDF generation failed for ULR: {}", labReport.getUlrNumber(), e);
                return generateMinimalPdf(labReport);
            }
        });
    }

    /**
     * Fallback method for PDF with barcodes generation failures
     * Returns a PDF without barcodes
     */
    public CompletableFuture<byte[]> fallbackGeneratePdfWithBarcodes(LabReport labReport, Exception ex) {
        logger.warn("PDF with barcodes generation fallback triggered for ULR: {} due to: {}", 
            labReport.getUlrNumber(), ex.getMessage());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Try to generate PDF without barcodes
                return pdfReportService.generatePdfReport(labReport);
            } catch (Exception e) {
                logger.error("Fallback PDF generation also failed for ULR: {}", labReport.getUlrNumber(), e);
                return generateSimpleFallbackPdf(labReport);
            }
        });
    }

    /**
     * Fallback method for HTML generation failures
     * Returns a simple HTML report
     */
    public CompletableFuture<String> fallbackGenerateHtml(LabReport labReport, Exception ex) {
        logger.warn("HTML generation fallback triggered for ULR: {} due to: {}", 
            labReport.getUlrNumber(), ex.getMessage());
        
        return CompletableFuture.completedFuture(generateSimpleFallbackHtml(labReport));
    }

    /**
     * Generate a simple fallback PDF when primary generation fails
     */
    private byte[] generateSimpleFallbackPdf(LabReport labReport) {
        try {
            // Create a simple text-based PDF content
            String content = generateFallbackContent(labReport);
            
            // Convert to bytes (this is a simplified approach)
            // In a real implementation, you might use a simpler PDF library
            return content.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Simple fallback PDF generation failed", e);
            return generateMinimalPdf(labReport);
        }
    }

    /**
     * Generate a minimal PDF when all else fails
     */
    private byte[] generateMinimalPdf(LabReport labReport) {
        String minimalContent = String.format(
            "LAB REPORT - %s\nGenerated: %s\nStatus: Service Temporarily Unavailable\nPlease contact lab for full report.",
            labReport.getUlrNumber(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        return minimalContent.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate a simple fallback HTML report
     */
    private String generateSimpleFallbackHtml(LabReport labReport) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Lab Report - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }
                    .content { margin-top: 20px; }
                    .error { color: #d32f2f; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Laboratory Report</h1>
                    <p><strong>ULR Number:</strong> %s</p>
                    <p><strong>Generated:</strong> %s</p>
                </div>
                <div class="content">
                    <div class="error">
                        Service Temporarily Unavailable
                    </div>
                    <p>We apologize for the inconvenience. The full report generation service is currently unavailable.</p>
                    <p>Please contact the laboratory for your complete report.</p>
                    <p><strong>Report Status:</strong> %s</p>
                    <p><strong>Report Type:</strong> %s</p>
                </div>
            </body>
            </html>
            """,
            labReport.getUlrNumber(),
            labReport.getUlrNumber(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            labReport.getReportStatus(),
            labReport.getReportType()
        );
    }

    /**
     * Generate fallback content for reports
     */
    private String generateFallbackContent(LabReport labReport) {
        StringBuilder content = new StringBuilder();
        content.append("=== LABORATORY REPORT ===\n\n");
        content.append("ULR Number: ").append(labReport.getUlrNumber()).append("\n");
        content.append("Report Status: ").append(labReport.getReportStatus()).append("\n");
        content.append("Report Type: ").append(labReport.getReportType()).append("\n");
        content.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        content.append("=== NOTICE ===\n");
        content.append("This is a simplified report generated due to service limitations.\n");
        content.append("Please contact the laboratory for the complete report with all details.\n\n");
        
        if (labReport.getReportData() != null) {
            content.append("=== REPORT DATA ===\n");
            content.append(labReport.getReportData().toString()).append("\n\n");
        }
        
        content.append("=== END OF REPORT ===\n");
        
        return content.toString();
    }

    /**
     * Health check method to verify PDF service availability
     */
    @CircuitBreaker(name = "pdfGeneration")
    public boolean isHealthy() {
        try {
            // Create a minimal test report
            LabReport testReport = new LabReport();
            testReport.setUlrNumber("HEALTH_CHECK");
            testReport.setReportStatus(com.sivalab.laboperations.entity.ReportStatus.DRAFT);
            testReport.setReportType(com.sivalab.laboperations.entity.ReportType.STANDARD);
            
            // Try to generate HTML (lighter than PDF)
            String testHtml = pdfReportService.generateHtmlReport(testReport);
            return testHtml != null && !testHtml.isEmpty();
        } catch (Exception e) {
            logger.warn("PDF service health check failed", e);
            return false;
        }
    }

    /**
     * Get service metrics
     */
    public ServiceMetrics getMetrics() {
        // This would typically integrate with Micrometer metrics
        return new ServiceMetrics(
            isHealthy(),
            System.currentTimeMillis(),
            "PDF Report Service"
        );
    }

    /**
     * Simple metrics class
     */
    public static class ServiceMetrics {
        private final boolean healthy;
        private final long timestamp;
        private final String serviceName;

        public ServiceMetrics(boolean healthy, long timestamp, String serviceName) {
            this.healthy = healthy;
            this.timestamp = timestamp;
            this.serviceName = serviceName;
        }

        public boolean isHealthy() { return healthy; }
        public long getTimestamp() { return timestamp; }
        public String getServiceName() { return serviceName; }
    }
}
