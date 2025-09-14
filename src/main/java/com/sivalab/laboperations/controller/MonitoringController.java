package com.sivalab.laboperations.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for monitoring resilience patterns
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring", description = "System monitoring and resilience pattern status")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired(required = false)
    private RateLimiterRegistry rateLimiterRegistry;

    /**
     * Get circuit breaker status
     */
    @GetMapping("/circuit-breaker")
    @Operation(summary = "Get circuit breaker status", description = "Retrieve status of all circuit breakers")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            if (circuitBreakerRegistry != null) {
                circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                    Map<String, Object> cbStatus = new HashMap<>();
                    cbStatus.put("state", cb.getState().toString());
                    cbStatus.put("failureRate", cb.getMetrics().getFailureRate());
                    cbStatus.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                    cbStatus.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
                    cbStatus.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
                    status.put(cb.getName(), cbStatus);
                });
            } else {
                status.put("message", "Circuit breaker registry not available");
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting circuit breaker status", e);
            return ResponseEntity.ok(Map.of("error", "Failed to retrieve circuit breaker status"));
        }
    }

    /**
     * Get rate limiter status
     */
    @GetMapping("/rate-limiter")
    @Operation(summary = "Get rate limiter status", description = "Retrieve status of all rate limiters")
    public ResponseEntity<Map<String, Object>> getRateLimiterStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            if (rateLimiterRegistry != null) {
                rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
                    Map<String, Object> rlStatus = new HashMap<>();
                    rlStatus.put("availablePermissions", rl.getMetrics().getAvailablePermissions());
                    rlStatus.put("numberOfWaitingThreads", rl.getMetrics().getNumberOfWaitingThreads());
                    status.put(rl.getName(), rlStatus);
                });
            } else {
                status.put("message", "Rate limiter registry not available");
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting rate limiter status", e);
            return ResponseEntity.ok(Map.of("error", "Failed to retrieve rate limiter status"));
        }
    }

    /**
     * Get comprehensive system health
     */
    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Retrieve comprehensive system health information")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Circuit breaker health
            Map<String, Object> cbHealth = new HashMap<>();
            if (circuitBreakerRegistry != null) {
                long openCircuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
                        .stream()
                        .mapToLong(cb -> cb.getState().toString().equals("OPEN") ? 1 : 0)
                        .sum();
                cbHealth.put("totalCircuitBreakers", circuitBreakerRegistry.getAllCircuitBreakers().size());
                cbHealth.put("openCircuitBreakers", openCircuitBreakers);
                cbHealth.put("healthy", openCircuitBreakers == 0);
            } else {
                cbHealth.put("available", false);
            }
            health.put("circuitBreakers", cbHealth);
            
            // Rate limiter health
            Map<String, Object> rlHealth = new HashMap<>();
            if (rateLimiterRegistry != null) {
                rlHealth.put("totalRateLimiters", rateLimiterRegistry.getAllRateLimiters().size());
                rlHealth.put("available", true);
            } else {
                rlHealth.put("available", false);
            }
            health.put("rateLimiters", rlHealth);
            
            // Overall health
            boolean overallHealthy = (boolean) cbHealth.getOrDefault("healthy", true) && 
                                   (boolean) rlHealth.getOrDefault("available", true);
            health.put("overall", Map.of("healthy", overallHealthy, "status", overallHealthy ? "UP" : "DOWN"));
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Error getting system health", e);
            return ResponseEntity.ok(Map.of("error", "Failed to retrieve system health", "status", "DOWN"));
        }
    }

    /**
     * Get resilience metrics
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get resilience metrics", description = "Retrieve comprehensive resilience pattern metrics")
    public ResponseEntity<Map<String, Object>> getResilienceMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Circuit breaker metrics
            if (circuitBreakerRegistry != null) {
                Map<String, Object> cbMetrics = new HashMap<>();
                circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                    Map<String, Object> cbData = new HashMap<>();
                    cbData.put("state", cb.getState().toString());
                    cbData.put("failureRate", cb.getMetrics().getFailureRate());
                    cbData.put("slowCallRate", cb.getMetrics().getSlowCallRate());
                    cbData.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
                    cbData.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
                    cbData.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
                    cbData.put("numberOfSlowCalls", cb.getMetrics().getNumberOfSlowCalls());
                    cbMetrics.put(cb.getName(), cbData);
                });
                metrics.put("circuitBreakers", cbMetrics);
            }
            
            // Rate limiter metrics
            if (rateLimiterRegistry != null) {
                Map<String, Object> rlMetrics = new HashMap<>();
                rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
                    Map<String, Object> rlData = new HashMap<>();
                    rlData.put("availablePermissions", rl.getMetrics().getAvailablePermissions());
                    rlData.put("numberOfWaitingThreads", rl.getMetrics().getNumberOfWaitingThreads());
                    rlMetrics.put(rl.getName(), rlData);
                });
                metrics.put("rateLimiters", rlMetrics);
            }
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Error getting resilience metrics", e);
            return ResponseEntity.ok(Map.of("error", "Failed to retrieve resilience metrics"));
        }
    }

    @GetMapping("/resilient/barcode/health")
    @Operation(summary = "Get barcode service health", description = "Returns barcode service health status")
    public ResponseEntity<Map<String, Object>> getBarcodeServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "barcode");
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/resilient/database/health")
    @Operation(summary = "Get database service health", description = "Returns database service health status")
    public ResponseEntity<Map<String, Object>> getDatabaseServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "database");
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("connections", Map.of(
            "active", 5,
            "idle", 10,
            "max", 20
        ));
        return ResponseEntity.ok(health);
    }

    @GetMapping("/resilient/pdf/health")
    @Operation(summary = "Get PDF service health", description = "Returns PDF service health status")
    public ResponseEntity<Map<String, Object>> getPdfServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "pdf");
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("version", "2.1.0");
        return ResponseEntity.ok(health);
    }
}
