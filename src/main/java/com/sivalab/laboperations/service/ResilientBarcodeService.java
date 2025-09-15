package com.sivalab.laboperations.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Resilient Barcode Service with Fault Tolerance Patterns
 * Wraps the original BarcodeService with circuit breaker, retry, rate limiting, etc.
 */
@Service
public class ResilientBarcodeService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientBarcodeService.class);

    @Autowired
    private BarcodeService barcodeService;

    /**
     * Generate QR Code with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackQRCode")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<byte[]> generateQRCodeResilient(String data, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating QR code for data: {} with size: {}", data, size);
                byte[] result = barcodeService.generateQRCode(data, size);
                logger.debug("Successfully generated QR code of {} bytes", result.length);
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate QR code for data: {}", data, e);
                throw new RuntimeException("QR code generation failed", e);
            }
        });
    }

    /**
     * Generate Code128 Barcode with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackBarcode")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<byte[]> generateCode128BarcodeResilient(String data, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating Code128 barcode for data: {} with dimensions: {}x{}", data, width, height);
                byte[] result = barcodeService.generateCode128Barcode(data, width, height);
                logger.debug("Successfully generated Code128 barcode of {} bytes", result.length);
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate Code128 barcode for data: {}", data, e);
                throw new RuntimeException("Code128 barcode generation failed", e);
            }
        });
    }

    /**
     * Generate Code39 Barcode with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackBarcode")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<byte[]> generateCode39BarcodeResilient(String data, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating Code39 barcode for data: {} with dimensions: {}x{}", data, width, height);
                byte[] result = barcodeService.generateCode39Barcode(data, width, height);
                logger.debug("Successfully generated Code39 barcode of {} bytes", result.length);
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate Code39 barcode for data: {}", data, e);
                throw new RuntimeException("Code39 barcode generation failed", e);
            }
        });
    }

    /**
     * Generate Visit QR Code with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackVisitQRCode")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<byte[]> generateVisitQRCodeResilient(Long visitId, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating visit QR code for visitId: {} with size: {}", visitId, size);
                String visitData = "VISIT_ID:" + visitId + "_SIZE:" + size + "_TIMESTAMP:" + System.currentTimeMillis();
                byte[] result = barcodeService.generateQRCode(visitData, size);
                logger.debug("Successfully generated visit QR code of {} bytes", result.length);
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate visit QR code for visitId: {}", visitId, e);
                throw new RuntimeException("Visit QR code generation failed", e);
            }
        });
    }

    /**
     * Generate Sample QR Code with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackSampleQRCode")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<byte[]> generateSampleQRCodeResilient(String sampleNumber, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating sample QR code for sampleNumber: {} with size: {}", sampleNumber, size);
                String sampleData = "SAMPLE:" + sampleNumber + "_SIZE:" + size + "_TIMESTAMP:" + System.currentTimeMillis();
                byte[] result = barcodeService.generateQRCode(sampleData, size);
                logger.debug("Successfully generated sample QR code of {} bytes", result.length);
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate sample QR code for sampleNumber: {}", sampleNumber, e);
                throw new RuntimeException("Sample QR code generation failed", e);
            }
        });
    }

    /**
     * Generate Report Barcode Package with fault tolerance
     */
    @CircuitBreaker(name = "barcodeGeneration", fallbackMethod = "fallbackBarcodePackage")
    @Retry(name = "externalService")
    @RateLimiter(name = "barcode")
    @Bulkhead(name = "barcode")
    @TimeLimiter(name = "barcode")
    public CompletableFuture<Map<String, byte[]>> generateReportBarcodePackageResilient(
            String ulrNumber, String patientName, String patientId, String reportStatus) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating report barcode package for ULR: {}", ulrNumber);
                Map<String, byte[]> result = barcodeService.generateReportBarcodePackage(
                    ulrNumber, patientName, patientId, reportStatus);
                logger.debug("Successfully generated report barcode package with {} items", result.size());
                return result;
            } catch (Exception e) {
                logger.error("Failed to generate report barcode package for ULR: {}", ulrNumber, e);
                throw new RuntimeException("Report barcode package generation failed", e);
            }
        });
    }

    // Fallback Methods

    /**
     * Fallback method for QR code generation failures
     */
    public CompletableFuture<byte[]> fallbackQRCode(String data, int size, Exception ex) {
        logger.warn("QR code generation fallback triggered for data: {} due to: {}", data, ex.getMessage());
        return CompletableFuture.completedFuture(generateErrorQRCode(data, size));
    }

    public CompletableFuture<byte[]> fallbackVisitQRCode(Long visitId, int size, Exception ex) {
        logger.warn("Visit QR code generation fallback triggered for visitId: {} due to: {}", visitId, ex.getMessage());
        return CompletableFuture.completedFuture(generateErrorQRCode("VISIT_" + visitId, size));
    }

    public CompletableFuture<byte[]> fallbackSampleQRCode(String sampleNumber, int size, Exception ex) {
        logger.warn("Sample QR code generation fallback triggered for sample: {} due to: {}", sampleNumber, ex.getMessage());
        return CompletableFuture.completedFuture(generateErrorQRCode("SAMPLE_" + sampleNumber, size));
    }

    /**
     * Fallback method for barcode generation failures
     */
    public CompletableFuture<byte[]> fallbackBarcode(String data, int width, int height, Exception ex) {
        logger.warn("Barcode generation fallback triggered for data: {} due to: {}", data, ex.getMessage());
        return CompletableFuture.completedFuture(generateErrorBarcode(data, width, height));
    }

    /**
     * Fallback method for barcode package generation failures
     */
    public CompletableFuture<Map<String, byte[]>> fallbackBarcodePackage(
            String ulrNumber, String patientName, String patientId, String reportStatus, Exception ex) {
        logger.warn("Barcode package generation fallback triggered for ULR: {} due to: {}", ulrNumber, ex.getMessage());
        return CompletableFuture.completedFuture(Map.of(
            "qrCode", generateErrorQRCode("ULR_" + ulrNumber, 200),
            "barcode", generateErrorBarcode(ulrNumber, 200, 50)
        ));
    }

    /**
     * Generate a simple error QR code when primary generation fails
     */
    private byte[] generateErrorQRCode(String data, int size) {
        try {
            // Generate a simple QR code with error message
            String errorData = "ERROR_GENERATING_QR_FOR_" + data;
            return barcodeService.generateQRCode(errorData, Math.max(size, 100));
        } catch (Exception e) {
            logger.error("Even fallback QR code generation failed", e);
            // Return a minimal placeholder
            return new byte[]{-119, 80, 78, 71, 13, 10, 26, 10}; // PNG header
        }
    }

    /**
     * Generate a simple error barcode when primary generation fails
     */
    private byte[] generateErrorBarcode(String data, int width, int height) {
        try {
            // Generate a simple barcode with error message
            String errorData = "ERROR_" + data.substring(0, Math.min(data.length(), 10));
            return barcodeService.generateCode128Barcode(errorData, Math.max(width, 100), Math.max(height, 30));
        } catch (Exception e) {
            logger.error("Even fallback barcode generation failed", e);
            // Return a minimal placeholder
            return new byte[]{-119, 80, 78, 71, 13, 10, 26, 10}; // PNG header
        }
    }

    /**
     * Health check method to verify barcode service availability
     */
    @CircuitBreaker(name = "barcodeGeneration")
    public boolean isHealthy() {
        try {
            // Try to generate a simple test QR code
            byte[] testQR = barcodeService.generateQRCode("HEALTH_CHECK", 100);
            return testQR != null && testQR.length > 0;
        } catch (Exception e) {
            logger.warn("Barcode service health check failed", e);
            return false;
        }
    }
}
