package com.sivalab.laboperations.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * System Health Service
 * Monitors the health of all system components and provides comprehensive health reporting
 */
@Service
public class SystemHealthService implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(SystemHealthService.class);

    @Autowired
    private ResilientBarcodeService resilientBarcodeService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Spring Boot Actuator health check implementation
     */
    @Override
    public Health health() {
        try {
            SystemHealthReport healthReport = getSystemHealthReport();
            
            if (healthReport.isOverallHealthy()) {
                return Health.up()
                    .withDetail("status", "All systems operational")
                    .withDetail("timestamp", healthReport.getTimestamp())
                    .withDetail("components", healthReport.getComponentHealth())
                    .withDetail("circuitBreakers", healthReport.getCircuitBreakerStatus())
                    .build();
            } else {
                return Health.down()
                    .withDetail("status", "Some systems are degraded")
                    .withDetail("timestamp", healthReport.getTimestamp())
                    .withDetail("components", healthReport.getComponentHealth())
                    .withDetail("circuitBreakers", healthReport.getCircuitBreakerStatus())
                    .withDetail("issues", healthReport.getIssues())
                    .build();
            }
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return Health.down()
                .withDetail("status", "Health check system failure")
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        }
    }

    /**
     * Get comprehensive system health report
     */
    public SystemHealthReport getSystemHealthReport() {
        logger.debug("Generating system health report");
        long startTime = System.currentTimeMillis();

        SystemHealthReport report = new SystemHealthReport();
        
        // Check barcode service health
        checkBarcodeServiceHealth(report);

        // Check basic system health
        checkBasicSystemHealth(report);
        
        // Check circuit breaker states
        checkCircuitBreakerStates(report);
        
        // Calculate overall health
        report.calculateOverallHealth();
        
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("System health report generated in {}ms, overall healthy: {}", 
            duration, report.isOverallHealthy());
        
        return report;
    }

    /**
     * Check basic system health
     */
    private void checkBasicSystemHealth(SystemHealthReport report) {
        try {
            // Check JVM health
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

            boolean memoryHealthy = memoryUsagePercent < 90; // Consider unhealthy if >90% memory used

            report.addComponentHealth("system", memoryHealthy,
                memoryHealthy ? "Healthy" : "High memory usage", Map.of(
                    "memoryUsagePercent", String.format("%.1f%%", memoryUsagePercent),
                    "totalMemoryMB", totalMemory / 1024 / 1024,
                    "usedMemoryMB", usedMemory / 1024 / 1024,
                    "availableProcessors", runtime.availableProcessors()
                ));

            if (!memoryHealthy) {
                report.addIssue("High memory usage: " + String.format("%.1f%%", memoryUsagePercent));
            }

        } catch (Exception e) {
            logger.error("Basic system health check failed", e);
            report.addComponentHealth("system", false, "Health check failed: " + e.getMessage(), null);
            report.addIssue("System health check exception: " + e.getMessage());
        }
    }

    /**
     * Check barcode service health
     */
    private void checkBarcodeServiceHealth(SystemHealthReport report) {
        try {
            boolean barcodeHealthy = resilientBarcodeService.isHealthy();
            
            if (barcodeHealthy) {
                report.addComponentHealth("barcodeService", true, "Operational", Map.of(
                    "qrCodeGeneration", "Available",
                    "barcodeGeneration", "Available",
                    "formats", "QR, Code128, Code39"
                ));
            } else {
                report.addComponentHealth("barcodeService", false, "Service degraded", null);
                report.addIssue("Barcode service health check failed");
            }
        } catch (Exception e) {
            logger.error("Barcode service health check failed", e);
            report.addComponentHealth("barcodeService", false, "Health check failed: " + e.getMessage(), null);
            report.addIssue("Barcode service health check exception: " + e.getMessage());
        }
    }



    /**
     * Check circuit breaker states
     */
    private void checkCircuitBreakerStates(SystemHealthReport report) {
        try {
            Map<String, String> circuitBreakerStates = new HashMap<>();
            
            // Check all registered circuit breakers
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                CircuitBreaker.State state = cb.getState();
                circuitBreakerStates.put(cb.getName(), state.toString());
                
                // Add issues for non-closed circuit breakers
                if (state != CircuitBreaker.State.CLOSED) {
                    report.addIssue("Circuit breaker '" + cb.getName() + "' is " + state);
                }
            });
            
            report.setCircuitBreakerStatus(circuitBreakerStates);
            
        } catch (Exception e) {
            logger.error("Circuit breaker state check failed", e);
            report.addIssue("Circuit breaker state check failed: " + e.getMessage());
        }
    }

    /**
     * Get detailed system metrics
     */
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        
        try {
            // JVM metrics
            Runtime runtime = Runtime.getRuntime();
            metrics.setTotalMemory(runtime.totalMemory());
            metrics.setFreeMemory(runtime.freeMemory());
            metrics.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());
            metrics.setMaxMemory(runtime.maxMemory());
            metrics.setAvailableProcessors(runtime.availableProcessors());
            
            // System uptime (approximate)
            metrics.setUptime(System.currentTimeMillis());
            
            // Thread information
            metrics.setActiveThreads(Thread.activeCount());
            
            logger.debug("System metrics collected: {}", metrics);
            
        } catch (Exception e) {
            logger.error("Failed to collect system metrics", e);
        }
        
        return metrics;
    }

    /**
     * System Health Report class
     */
    public static class SystemHealthReport {
        private final Map<String, ComponentHealth> componentHealth = new HashMap<>();
        private final Map<String, String> circuitBreakerStatus = new HashMap<>();
        private final java.util.List<String> issues = new java.util.ArrayList<>();
        private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        private boolean overallHealthy = true;

        public void addComponentHealth(String component, boolean healthy, String status, Map<String, Object> details) {
            componentHealth.put(component, new ComponentHealth(healthy, status, details));
            if (!healthy) {
                overallHealthy = false;
            }
        }

        public void addIssue(String issue) {
            issues.add(issue);
        }

        public void setCircuitBreakerStatus(Map<String, String> status) {
            circuitBreakerStatus.putAll(status);
        }

        public void calculateOverallHealth() {
            // Overall health is false if any component is unhealthy or there are issues
            overallHealthy = componentHealth.values().stream().allMatch(ComponentHealth::isHealthy) && issues.isEmpty();
        }

        // Getters
        public Map<String, ComponentHealth> getComponentHealth() { return componentHealth; }
        public Map<String, String> getCircuitBreakerStatus() { return circuitBreakerStatus; }
        public java.util.List<String> getIssues() { return issues; }
        public String getTimestamp() { return timestamp; }
        public boolean isOverallHealthy() { return overallHealthy; }
    }

    /**
     * Component Health class
     */
    public static class ComponentHealth {
        private final boolean healthy;
        private final String status;
        private final Map<String, Object> details;

        public ComponentHealth(boolean healthy, String status, Map<String, Object> details) {
            this.healthy = healthy;
            this.status = status;
            this.details = details;
        }

        public boolean isHealthy() { return healthy; }
        public String getStatus() { return status; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * System Metrics class
     */
    public static class SystemMetrics {
        private long totalMemory;
        private long freeMemory;
        private long usedMemory;
        private long maxMemory;
        private int availableProcessors;
        private long uptime;
        private int activeThreads;

        // Getters and setters
        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { this.availableProcessors = availableProcessors; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }

        @Override
        public String toString() {
            return String.format("SystemMetrics{memory=%d/%d MB, processors=%d, threads=%d}", 
                usedMemory / 1024 / 1024, maxMemory / 1024 / 1024, availableProcessors, activeThreads);
        }
    }
}
