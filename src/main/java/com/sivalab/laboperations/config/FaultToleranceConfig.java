package com.sivalab.laboperations.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Fault Tolerance Configuration for Lab Operations System
 * Implements Circuit Breaker, Retry, Rate Limiting, Bulkhead, and Time Limiter patterns
 */
@Configuration
public class FaultToleranceConfig {

    /**
     * Circuit Breaker for Database Operations
     * Prevents cascading failures when database is unavailable
     */
    @Bean
    public CircuitBreaker databaseCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Open circuit if 50% of calls fail
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
                .slidingWindowSize(10) // Consider last 10 calls
                .minimumNumberOfCalls(5) // Need at least 5 calls to calculate failure rate
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
                .slowCallRateThreshold(50) // Consider slow calls as failures
                .slowCallDurationThreshold(Duration.ofSeconds(2)) // Calls taking >2s are slow
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        return CircuitBreaker.of("database", config);
    }

    /**
     * Circuit Breaker for PDF Generation
     * Protects against PDF generation service failures
     */
    @Bean
    public CircuitBreaker pdfGenerationCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60) // PDF generation can be more tolerant
                .waitDurationInOpenState(Duration.ofSeconds(60)) // Longer wait for PDF service
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .slowCallRateThreshold(70)
                .slowCallDurationThreshold(Duration.ofSeconds(5)) // PDF generation can take longer
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        return CircuitBreaker.of("pdfGeneration", config);
    }

    /**
     * Circuit Breaker for Barcode Generation
     * Protects against barcode service failures
     */
    @Bean
    public CircuitBreaker barcodeGenerationCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(40) // Barcode generation should be highly reliable
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(15)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallRateThreshold(40)
                .slowCallDurationThreshold(Duration.ofSeconds(1)) // Barcode generation should be fast
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        
        return CircuitBreaker.of("barcodeGeneration", config);
    }

    /**
     * Retry Configuration for Database Operations
     * Handles transient database failures
     */
    @Bean
    public Retry databaseRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // Retry up to 3 times
                .waitDuration(Duration.ofMillis(500)) // Wait 500ms between retries
                .retryOnException(throwable ->
                    throwable instanceof org.springframework.dao.DataAccessException ||
                    throwable instanceof java.sql.SQLException ||
                    throwable instanceof org.springframework.transaction.TransactionException)
                .build();
        
        return Retry.of("database", config);
    }

    /**
     * Retry Configuration for External Services
     * Handles transient failures in external service calls
     */
    @Bean
    public Retry externalServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(2) // Fewer retries for external services
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(throwable ->
                    throwable instanceof java.io.IOException ||
                    throwable instanceof java.net.SocketTimeoutException ||
                    throwable instanceof java.net.ConnectException)
                .build();
        
        return Retry.of("externalService", config);
    }

    /**
     * Rate Limiter for API Endpoints
     * Prevents API abuse and ensures fair resource usage
     */
    @Bean
    public RateLimiter apiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // Allow 100 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Per minute
                .timeoutDuration(Duration.ofSeconds(5)) // Wait up to 5s for permission
                .build();
        
        return RateLimiter.of("api", config);
    }

    /**
     * Rate Limiter for Barcode Generation
     * Prevents excessive barcode generation requests
     */
    @Bean
    public RateLimiter barcodeRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(50) // Allow 50 barcode generations
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Per minute
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        
        return RateLimiter.of("barcode", config);
    }

    /**
     * Rate Limiter for PDF Generation
     * Prevents excessive PDF generation requests (resource intensive)
     */
    @Bean
    public RateLimiter pdfRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(20) // Allow 20 PDF generations
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Per minute
                .timeoutDuration(Duration.ofSeconds(10)) // Longer timeout for PDF
                .build();
        
        return RateLimiter.of("pdf", config);
    }

    /**
     * Bulkhead for Database Operations
     * Isolates database operations to prevent resource exhaustion
     */
    @Bean
    public Bulkhead databaseBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10) // Allow max 10 concurrent database calls
                .maxWaitDuration(Duration.ofSeconds(5)) // Wait up to 5s for slot
                .build();
        
        return Bulkhead.of("database", config);
    }

    /**
     * Bulkhead for PDF Generation
     * Isolates PDF generation to prevent memory exhaustion
     */
    @Bean
    public Bulkhead pdfBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(3) // Allow max 3 concurrent PDF generations
                .maxWaitDuration(Duration.ofSeconds(15)) // Longer wait for PDF
                .build();
        
        return Bulkhead.of("pdf", config);
    }

    /**
     * Bulkhead for Barcode Generation
     * Isolates barcode generation operations
     */
    @Bean
    public Bulkhead barcodeBulkhead() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5) // Allow max 5 concurrent barcode generations
                .maxWaitDuration(Duration.ofSeconds(3))
                .build();
        
        return Bulkhead.of("barcode", config);
    }

    /**
     * Time Limiter for Database Operations
     * Prevents long-running database operations from blocking threads
     */
    @Bean
    public TimeLimiter databaseTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // Database operations should complete within 10s
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("database", config);
    }

    /**
     * Time Limiter for PDF Generation
     * Prevents PDF generation from taking too long
     */
    @Bean
    public TimeLimiter pdfTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30)) // PDF generation can take up to 30s
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("pdf", config);
    }

    /**
     * Time Limiter for Barcode Generation
     * Ensures barcode generation completes quickly
     */
    @Bean
    public TimeLimiter barcodeTimeLimiter() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5)) // Barcode generation should be fast
                .cancelRunningFuture(true)
                .build();
        
        return TimeLimiter.of("barcode", config);
    }
}
