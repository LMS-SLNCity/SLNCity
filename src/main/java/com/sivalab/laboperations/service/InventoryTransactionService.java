package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.InventoryTransaction;
import com.sivalab.laboperations.repository.InventoryTransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing inventory transactions
 */
@Service
@Transactional
public class InventoryTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryTransactionService.class);

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    /**
     * Create new inventory transaction
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCreateTransaction")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public InventoryTransaction createTransaction(InventoryTransaction transaction) {
        logger.info("Creating new inventory transaction for item: {}", 
                   transaction.getInventoryItem().getName());
        return transactionRepository.save(transaction);
    }

    /**
     * Get transaction by ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetTransaction")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<InventoryTransaction> getTransactionById(Long id) {
        logger.debug("Fetching transaction with ID: {}", id);
        return transactionRepository.findById(id);
    }

    /**
     * Get all transactions
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetAllTransactions")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryTransaction> getAllTransactions() {
        logger.debug("Fetching all transactions");
        return transactionRepository.findAll();
    }

    /**
     * Get transactions by inventory item
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetTransactionsByItem")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryTransaction> getTransactionsByInventoryItemId(Long inventoryItemId) {
        logger.debug("Fetching transactions for inventory item ID: {}", inventoryItemId);
        return transactionRepository.findByInventoryItemIdOrderByTransactionDateDesc(inventoryItemId);
    }

    /**
     * Get transactions by type
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetTransactionsByType")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryTransaction> getTransactionsByType(InventoryTransaction.TransactionType transactionType) {
        logger.debug("Fetching transactions with type: {}", transactionType);
        return transactionRepository.findByTransactionType(transactionType);
    }

    /**
     * Get transactions within date range
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetTransactionsByDateRange")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(startDate, endDate);
    }

    /**
     * Get transactions with pagination
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetTransactionsWithPagination")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Page<InventoryTransaction> getTransactionsWithPagination(Pageable pageable) {
        logger.debug("Fetching transactions with pagination");
        return transactionRepository.findAllByOrderByTransactionDateDesc(pageable);
    }

    /**
     * Get recent transactions
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetRecentTransactions")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<InventoryTransaction> getRecentTransactions(int limit) {
        logger.debug("Fetching {} most recent transactions", limit);
        return transactionRepository.findTopNByOrderByTransactionDateDesc(limit);
    }

    /**
     * Delete transaction
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackDeleteTransaction")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public void deleteTransaction(Long id) {
        logger.info("Deleting transaction with ID: {}", id);
        
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction not found with ID: " + id);
        }
        
        transactionRepository.deleteById(id);
    }

    // Fallback methods
    public InventoryTransaction fallbackCreateTransaction(InventoryTransaction transaction, Exception ex) {
        logger.error("Fallback: Failed to create inventory transaction", ex);
        throw new RuntimeException("Transaction service temporarily unavailable");
    }

    public Optional<InventoryTransaction> fallbackGetTransaction(Long id, Exception ex) {
        logger.error("Fallback: Failed to get transaction by ID", ex);
        return Optional.empty();
    }

    public List<InventoryTransaction> fallbackGetAllTransactions(Exception ex) {
        logger.error("Fallback: Failed to get all transactions", ex);
        return List.of();
    }

    public List<InventoryTransaction> fallbackGetTransactionsByItem(Long inventoryItemId, Exception ex) {
        logger.error("Fallback: Failed to get transactions by item", ex);
        return List.of();
    }

    public List<InventoryTransaction> fallbackGetTransactionsByType(InventoryTransaction.TransactionType transactionType, Exception ex) {
        logger.error("Fallback: Failed to get transactions by type", ex);
        return List.of();
    }

    public List<InventoryTransaction> fallbackGetTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Exception ex) {
        logger.error("Fallback: Failed to get transactions by date range", ex);
        return List.of();
    }

    public Page<InventoryTransaction> fallbackGetTransactionsWithPagination(Pageable pageable, Exception ex) {
        logger.error("Fallback: Failed to get transactions with pagination", ex);
        return Page.empty();
    }

    public List<InventoryTransaction> fallbackGetRecentTransactions(int limit, Exception ex) {
        logger.error("Fallback: Failed to get recent transactions", ex);
        return List.of();
    }

    public void fallbackDeleteTransaction(Long id, Exception ex) {
        logger.error("Fallback: Failed to delete transaction", ex);
        throw new RuntimeException("Transaction service temporarily unavailable");
    }
}
