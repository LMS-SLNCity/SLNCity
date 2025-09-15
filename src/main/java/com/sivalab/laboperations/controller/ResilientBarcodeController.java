package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.service.ResilientBarcodeService;
import com.sivalab.laboperations.service.SystemHealthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Resilient Barcode Controller with Fault Tolerance
 * Provides barcode generation endpoints with comprehensive error handling and fault tolerance
 */
@RestController
@RequestMapping("/api/v1/resilient/barcodes")
@CrossOrigin(origins = "*")
public class ResilientBarcodeController {

    private static final Logger logger = LoggerFactory.getLogger(ResilientBarcodeController.class);

    @Autowired
    private ResilientBarcodeService resilientBarcodeService;

    @Autowired
    private SystemHealthService systemHealthService;

    /**
     * Generate QR Code with fault tolerance
     */
    @PostMapping("/qr")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<byte[]>> generateQRCode(@RequestBody Map<String, Object> request) {
        try {
            String data = (String) request.get("data");
            Integer size = (Integer) request.getOrDefault("size", 200);

            logger.info("Generating QR code for data length: {} with size: {}", data.length(), size);

            return resilientBarcodeService.generateQRCodeResilient(data, size)
                .thenApply(qrCodeBytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    headers.setContentLength(qrCodeBytes.length);
                    headers.setCacheControl("max-age=3600"); // Cache for 1 hour
                    
                    logger.info("Successfully generated QR code of {} bytes", qrCodeBytes.length);
                    return ResponseEntity.ok()
                        .headers(headers)
                        .body(qrCodeBytes);
                })
                .exceptionally(throwable -> {
                    logger.error("QR code generation failed", throwable);
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(("QR code generation failed: " + throwable.getMessage()).getBytes());
                });

        } catch (Exception e) {
            logger.error("QR code request processing failed", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * Generate Code128 Barcode with fault tolerance
     */
    @PostMapping("/code128")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<byte[]>> generateCode128Barcode(@RequestBody Map<String, Object> request) {
        try {
            String data = (String) request.get("data");
            Integer width = (Integer) request.getOrDefault("width", 200);
            Integer height = (Integer) request.getOrDefault("height", 50);

            logger.info("Generating Code128 barcode for data: {} with dimensions: {}x{}", data, width, height);

            return resilientBarcodeService.generateCode128BarcodeResilient(data, width, height)
                .thenApply(barcodeBytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    headers.setContentLength(barcodeBytes.length);
                    headers.setCacheControl("max-age=3600");
                    
                    logger.info("Successfully generated Code128 barcode of {} bytes", barcodeBytes.length);
                    return ResponseEntity.ok()
                        .headers(headers)
                        .body(barcodeBytes);
                })
                .exceptionally(throwable -> {
                    logger.error("Code128 barcode generation failed", throwable);
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(("Code128 barcode generation failed: " + throwable.getMessage()).getBytes());
                });

        } catch (Exception e) {
            logger.error("Code128 barcode request processing failed", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * Generate Code39 Barcode with fault tolerance
     */
    @PostMapping("/code39")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<byte[]>> generateCode39Barcode(@RequestBody Map<String, Object> request) {
        try {
            String data = (String) request.get("data");
            Integer width = (Integer) request.getOrDefault("width", 200);
            Integer height = (Integer) request.getOrDefault("height", 50);

            logger.info("Generating Code39 barcode for data: {} with dimensions: {}x{}", data, width, height);

            return resilientBarcodeService.generateCode39BarcodeResilient(data, width, height)
                .thenApply(barcodeBytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    headers.setContentLength(barcodeBytes.length);
                    headers.setCacheControl("max-age=3600");
                    
                    logger.info("Successfully generated Code39 barcode of {} bytes", barcodeBytes.length);
                    return ResponseEntity.ok()
                        .headers(headers)
                        .body(barcodeBytes);
                })
                .exceptionally(throwable -> {
                    logger.error("Code39 barcode generation failed", throwable);
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(("Code39 barcode generation failed: " + throwable.getMessage()).getBytes());
                });

        } catch (Exception e) {
            logger.error("Code39 barcode request processing failed", e);
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(("Invalid request: " + e.getMessage()).getBytes())
            );
        }
    }

    /**
     * Generate Visit QR Code with fault tolerance
     */
    @GetMapping("/visits/{visitId}/qr")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<byte[]>> generateVisitQRCode(
            @PathVariable Long visitId,
            @RequestParam(defaultValue = "200") int size) {
        
        logger.info("Generating visit QR code for visitId: {} with size: {}", visitId, size);

        return resilientBarcodeService.generateVisitQRCodeResilient(visitId, size)
            .thenApply(qrCodeBytes -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(qrCodeBytes.length);
                headers.setCacheControl("max-age=1800"); // Cache for 30 minutes
                
                logger.info("Successfully generated visit QR code of {} bytes", qrCodeBytes.length);
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCodeBytes);
            })
            .exceptionally(throwable -> {
                logger.error("Visit QR code generation failed for visitId: {}", visitId, throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(("Visit QR code generation failed: " + throwable.getMessage()).getBytes());
            });
    }

    /**
     * Generate Sample QR Code with fault tolerance
     */
    @GetMapping("/samples/{sampleNumber}/qr")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<byte[]>> generateSampleQRCode(
            @PathVariable String sampleNumber,
            @RequestParam(defaultValue = "200") int size) {
        
        logger.info("Generating sample QR code for sampleNumber: {} with size: {}", sampleNumber, size);

        return resilientBarcodeService.generateSampleQRCodeResilient(sampleNumber, size)
            .thenApply(qrCodeBytes -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(qrCodeBytes.length);
                headers.setCacheControl("max-age=1800");
                
                logger.info("Successfully generated sample QR code of {} bytes", qrCodeBytes.length);
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCodeBytes);
            })
            .exceptionally(throwable -> {
                logger.error("Sample QR code generation failed for sampleNumber: {}", sampleNumber, throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(("Sample QR code generation failed: " + throwable.getMessage()).getBytes());
            });
    }

    /**
     * Generate Report Barcode Package with fault tolerance
     */
    @GetMapping("/reports/{reportId}/package")
    @RateLimiter(name = "api")
    public CompletableFuture<ResponseEntity<Map<String, String>>> generateReportBarcodePackage(
            @PathVariable Long reportId,
            @RequestParam String ulrNumber,
            @RequestParam String patientName,
            @RequestParam String patientId,
            @RequestParam(defaultValue = "DRAFT") String reportStatus) {
        
        logger.info("Generating report barcode package for reportId: {} with ULR: {}", reportId, ulrNumber);

        return resilientBarcodeService.generateReportBarcodePackageResilient(ulrNumber, patientName, patientId, reportStatus)
            .thenApply(barcodePackage -> {
                // Convert byte arrays to base64 for JSON response
                Map<String, String> base64Package = new java.util.HashMap<>();
                barcodePackage.forEach((key, value) -> {
                    String base64 = java.util.Base64.getEncoder().encodeToString(value);
                    base64Package.put(key, base64);
                });
                
                logger.info("Successfully generated report barcode package with {} items", base64Package.size());
                return ResponseEntity.ok()
                    .cacheControl(org.springframework.http.CacheControl.maxAge(30, TimeUnit.MINUTES))
                    .body(base64Package);
            })
            .exceptionally(throwable -> {
                logger.error("Report barcode package generation failed for reportId: {}", reportId, throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Report barcode package generation failed: " + throwable.getMessage()));
            });
    }

    /**
     * Health check endpoint for barcode service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getBarcodeServiceHealth() {
        try {
            boolean healthy = resilientBarcodeService.isHealthy();
            SystemHealthService.SystemHealthReport systemHealth = systemHealthService.getSystemHealthReport();
            
            Map<String, Object> healthResponse = Map.of(
                "barcodeService", Map.of(
                    "healthy", healthy,
                    "status", healthy ? "Operational" : "Degraded"
                ),
                "systemHealth", Map.of(
                    "overallHealthy", systemHealth.isOverallHealthy(),
                    "timestamp", systemHealth.getTimestamp(),
                    "components", systemHealth.getComponentHealth()
                )
            );
            
            HttpStatus status = healthy && systemHealth.isOverallHealthy() ? 
                HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            return ResponseEntity.status(status).body(healthResponse);
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Health check failed: " + e.getMessage()));
        }
    }

    /**
     * Service metrics endpoint
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getBarcodeServiceMetrics() {
        try {
            SystemHealthService.SystemMetrics systemMetrics = systemHealthService.getSystemMetrics();
            
            Map<String, Object> metricsResponse = Map.of(
                "service", "Resilient Barcode Service",
                "timestamp", System.currentTimeMillis(),
                "systemMetrics", Map.of(
                    "memoryUsed", systemMetrics.getUsedMemory(),
                    "memoryMax", systemMetrics.getMaxMemory(),
                    "processors", systemMetrics.getAvailableProcessors(),
                    "activeThreads", systemMetrics.getActiveThreads()
                ),
                "endpoints", Map.of(
                    "qrGeneration", "/api/v1/resilient/barcodes/qr",
                    "code128Generation", "/api/v1/resilient/barcodes/code128",
                    "code39Generation", "/api/v1/resilient/barcodes/code39",
                    "visitQR", "/api/v1/resilient/barcodes/visits/{visitId}/qr",
                    "sampleQR", "/api/v1/resilient/barcodes/samples/{sampleNumber}/qr"
                )
            );
            
            return ResponseEntity.ok(metricsResponse);
            
        } catch (Exception e) {
            logger.error("Metrics collection failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Metrics collection failed: " + e.getMessage()));
        }
    }
}
