package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for InventoryTransaction entity
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Find transactions by inventory item ID ordered by date descending
     */
    List<InventoryTransaction> findByInventoryItemIdOrderByTransactionDateDesc(Long inventoryItemId);

    /**
     * Find transactions by transaction type
     */
    List<InventoryTransaction> findByTransactionType(InventoryTransaction.TransactionType transactionType);

    /**
     * Find transactions by performed by
     */
    List<InventoryTransaction> findByPerformedByContainingIgnoreCase(String performedBy);

    /**
     * Find transactions within date range
     */
    List<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find transactions by supplier
     */
    List<InventoryTransaction> findBySupplierContainingIgnoreCase(String supplier);

    /**
     * Find transactions by lot number
     */
    List<InventoryTransaction> findByLotNumber(String lotNumber);

    /**
     * Find transactions by reference number
     */
    List<InventoryTransaction> findByReferenceNumber(String referenceNumber);

    /**
     * Find all transactions ordered by date descending with pagination
     */
    Page<InventoryTransaction> findAllByOrderByTransactionDateDesc(Pageable pageable);

    /**
     * Find transactions by inventory item with pagination
     */
    Page<InventoryTransaction> findByInventoryItemIdOrderByTransactionDateDesc(Long inventoryItemId, Pageable pageable);

    /**
     * Find transactions by type with pagination
     */
    Page<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(
            InventoryTransaction.TransactionType transactionType, Pageable pageable);

    /**
     * Get top N recent transactions
     */
    @Query("SELECT t FROM InventoryTransaction t ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findTopNByOrderByTransactionDateDesc(@Param("limit") int limit);

    /**
     * Count transactions by type
     */
    long countByTransactionType(InventoryTransaction.TransactionType transactionType);

    /**
     * Count transactions by inventory item
     */
    long countByInventoryItemId(Long inventoryItemId);

    /**
     * Count transactions within date range
     */
    long countByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get transaction statistics by type
     */
    @Query("SELECT t.transactionType, COUNT(t), SUM(t.quantity) FROM InventoryTransaction t GROUP BY t.transactionType")
    List<Object[]> getTransactionStatisticsByType();

    /**
     * Get transaction statistics by date (daily)
     */
    @Query("SELECT DATE(t.transactionDate), COUNT(t), SUM(t.quantity) FROM InventoryTransaction t " +
           "WHERE t.transactionDate >= :startDate GROUP BY DATE(t.transactionDate) ORDER BY DATE(t.transactionDate)")
    List<Object[]> getDailyTransactionStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * Get transaction value statistics
     */
    @Query("SELECT t.transactionType, SUM(t.totalCost) FROM InventoryTransaction t " +
           "WHERE t.totalCost IS NOT NULL GROUP BY t.transactionType")
    List<Object[]> getTransactionValueStatistics();

    /**
     * Find stock in transactions within date range
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionType = 'STOCK_IN' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findStockInTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find stock out transactions within date range
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.transactionType = 'STOCK_OUT' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findStockOutTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions with total cost greater than specified amount
     */
    @Query("SELECT t FROM InventoryTransaction t WHERE t.totalCost > :amount ORDER BY t.totalCost DESC")
    List<InventoryTransaction> findTransactionsWithCostGreaterThan(@Param("amount") Double amount);

    /**
     * Get monthly transaction summary
     */
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), t.transactionType, " +
           "COUNT(t), SUM(t.quantity), SUM(t.totalCost) FROM InventoryTransaction t " +
           "WHERE t.transactionDate >= :startDate " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.transactionType " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> getMonthlyTransactionSummary(@Param("startDate") LocalDateTime startDate);
}
