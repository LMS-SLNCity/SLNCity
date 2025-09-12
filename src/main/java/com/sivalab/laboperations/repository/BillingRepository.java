package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    
    /**
     * Find billing by visit ID
     */
    Optional<Billing> findByVisitVisitId(Long visitId);
    
    /**
     * Find bills by payment status
     */
    List<Billing> findByPaid(Boolean paid);
    
    /**
     * Find bills created between dates
     */
    List<Billing> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find bills by amount range
     */
    List<Billing> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find unpaid bills older than specified date
     */
    @Query("SELECT b FROM Billing b WHERE b.paid = false AND b.createdAt < :date")
    List<Billing> findUnpaidBillsOlderThan(@Param("date") LocalDateTime date);
    
    /**
     * Calculate total revenue for paid bills
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.paid = true")
    BigDecimal calculateTotalRevenue();
    
    /**
     * Calculate total revenue for paid bills within date range
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.paid = true AND b.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Calculate total outstanding amount
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.paid = false")
    BigDecimal calculateOutstandingAmount();
    
    /**
     * Count bills by payment status
     */
    long countByPaid(Boolean paid);
}
