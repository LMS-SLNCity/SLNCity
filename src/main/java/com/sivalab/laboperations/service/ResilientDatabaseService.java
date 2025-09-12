package com.sivalab.laboperations.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Resilient Database Service with Fault Tolerance Patterns
 * Provides database operations with circuit breaker, retry, and bulkhead patterns
 */
@Service
public class ResilientDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(ResilientDatabaseService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * Execute query with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackQuery")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    public CompletableFuture<List<Map<String, Object>>> executeQueryResilient(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing query: {} with params: {}", sql, params);
                long startTime = System.currentTimeMillis();
                
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Query executed successfully in {}ms, returned {} rows", duration, result.size());
                
                return result;
            } catch (DataAccessException e) {
                logger.error("Database query failed: {}", sql, e);
                throw new RuntimeException("Database query execution failed", e);
            }
        });
    }

    /**
     * Execute update with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdate")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    @Transactional
    public CompletableFuture<Integer> executeUpdateResilient(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing update: {} with params: {}", sql, params);
                long startTime = System.currentTimeMillis();
                
                int result = jdbcTemplate.update(sql, params);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Update executed successfully in {}ms, affected {} rows", duration, result);
                
                return result;
            } catch (DataAccessException e) {
                logger.error("Database update failed: {}", sql, e);
                throw new RuntimeException("Database update execution failed", e);
            }
        });
    }

    /**
     * Execute batch update with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackBatchUpdate")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    @Transactional
    public CompletableFuture<int[]> executeBatchUpdateResilient(String sql, List<Object[]> batchParams) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing batch update: {} with {} batches", sql, batchParams.size());
                long startTime = System.currentTimeMillis();
                
                int[] result = jdbcTemplate.batchUpdate(sql, batchParams);
                
                long duration = System.currentTimeMillis() - startTime;
                int totalAffected = java.util.Arrays.stream(result).sum();
                logger.debug("Batch update executed successfully in {}ms, affected {} total rows", 
                    duration, totalAffected);
                
                return result;
            } catch (DataAccessException e) {
                logger.error("Database batch update failed: {}", sql, e);
                throw new RuntimeException("Database batch update execution failed", e);
            }
        });
    }

    /**
     * Get single value with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackSingleValue")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    public <T> CompletableFuture<T> queryForObjectResilient(String sql, Class<T> requiredType, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing single value query: {} for type: {}", sql, requiredType.getSimpleName());
                long startTime = System.currentTimeMillis();
                
                T result = jdbcTemplate.queryForObject(sql, requiredType, params);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Single value query executed successfully in {}ms", duration);
                
                return result;
            } catch (DataAccessException e) {
                logger.error("Database single value query failed: {}", sql, e);
                throw new RuntimeException("Database single value query execution failed", e);
            }
        });
    }

    /**
     * Check database connectivity with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackConnectivityCheck")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    public CompletableFuture<Boolean> checkConnectivityResilient() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Checking database connectivity");
                
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(5); // 5 second timeout
                    logger.debug("Database connectivity check result: {}", isValid);
                    return isValid;
                }
            } catch (SQLException e) {
                logger.error("Database connectivity check failed", e);
                throw new RuntimeException("Database connectivity check failed", e);
            }
        });
    }

    /**
     * Get database statistics with fault tolerance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackDatabaseStats")
    @Retry(name = "database")
    @Bulkhead(name = "database")
    @TimeLimiter(name = "database")
    public CompletableFuture<DatabaseStats> getDatabaseStatsResilient() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Retrieving database statistics");
                long startTime = System.currentTimeMillis();
                
                // Get table counts
                int visitCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Integer.class);
                int reportCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lab_reports", Integer.class);
                int sampleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM samples", Integer.class);
                int testCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lab_tests", Integer.class);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Database statistics retrieved successfully in {}ms", duration);
                
                return new DatabaseStats(visitCount, reportCount, sampleCount, testCount, 
                    System.currentTimeMillis(), true);
                
            } catch (DataAccessException e) {
                logger.error("Failed to retrieve database statistics", e);
                throw new RuntimeException("Database statistics retrieval failed", e);
            }
        });
    }

    // Fallback Methods

    /**
     * Fallback for query operations
     */
    public CompletableFuture<List<Map<String, Object>>> fallbackQuery(String sql, Object[] params, Exception ex) {
        logger.warn("Database query fallback triggered for SQL: {} due to: {}", sql, ex.getMessage());
        return CompletableFuture.completedFuture(List.of(Map.of("error", "Database temporarily unavailable")));
    }

    /**
     * Fallback for update operations
     */
    public CompletableFuture<Integer> fallbackUpdate(String sql, Object[] params, Exception ex) {
        logger.warn("Database update fallback triggered for SQL: {} due to: {}", sql, ex.getMessage());
        return CompletableFuture.completedFuture(-1); // Indicate failure
    }

    /**
     * Fallback for batch update operations
     */
    public CompletableFuture<int[]> fallbackBatchUpdate(String sql, List<Object[]> batchParams, Exception ex) {
        logger.warn("Database batch update fallback triggered for SQL: {} due to: {}", sql, ex.getMessage());
        int[] failureResult = new int[batchParams.size()];
        java.util.Arrays.fill(failureResult, -1);
        return CompletableFuture.completedFuture(failureResult);
    }

    /**
     * Fallback for single value queries
     */
    public <T> CompletableFuture<T> fallbackSingleValue(String sql, Class<T> requiredType, Object[] params, Exception ex) {
        logger.warn("Database single value query fallback triggered for SQL: {} due to: {}", sql, ex.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Fallback for connectivity check
     */
    public CompletableFuture<Boolean> fallbackConnectivityCheck(Exception ex) {
        logger.warn("Database connectivity check fallback triggered due to: {}", ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Fallback for database statistics
     */
    public CompletableFuture<DatabaseStats> fallbackDatabaseStats(Exception ex) {
        logger.warn("Database statistics fallback triggered due to: {}", ex.getMessage());
        return CompletableFuture.completedFuture(
            new DatabaseStats(0, 0, 0, 0, System.currentTimeMillis(), false)
        );
    }

    /**
     * Health check method
     */
    @CircuitBreaker(name = "database")
    public boolean isHealthy() {
        try {
            return checkConnectivityResilient().get();
        } catch (Exception e) {
            logger.warn("Database health check failed", e);
            return false;
        }
    }

    /**
     * Database statistics class
     */
    public static class DatabaseStats {
        private final int visitCount;
        private final int reportCount;
        private final int sampleCount;
        private final int testCount;
        private final long timestamp;
        private final boolean available;

        public DatabaseStats(int visitCount, int reportCount, int sampleCount, int testCount, 
                           long timestamp, boolean available) {
            this.visitCount = visitCount;
            this.reportCount = reportCount;
            this.sampleCount = sampleCount;
            this.testCount = testCount;
            this.timestamp = timestamp;
            this.available = available;
        }

        // Getters
        public int getVisitCount() { return visitCount; }
        public int getReportCount() { return reportCount; }
        public int getSampleCount() { return sampleCount; }
        public int getTestCount() { return testCount; }
        public long getTimestamp() { return timestamp; }
        public boolean isAvailable() { return available; }
        
        @Override
        public String toString() {
            return String.format("DatabaseStats{visits=%d, reports=%d, samples=%d, tests=%d, available=%s}", 
                visitCount, reportCount, sampleCount, testCount, available);
        }
    }
}
