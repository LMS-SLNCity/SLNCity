package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.BillingResponse;
import com.sivalab.laboperations.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing")
@Tag(name = "Billing Management", description = "Billing and payment management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;
    
    @Autowired
    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }
    
    /**
     * Generate bill for visit
     * GET /visits/{visitId}/bill
     */
    @GetMapping("/visits/{visitId}/bill")
    public ResponseEntity<BillingResponse> generateBill(@PathVariable Long visitId) {
        try {
            BillingResponse response = billingService.generateBill(visitId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("not approved") || e.getMessage().contains("already exists")) {
                return ResponseEntity.badRequest().build();
            }
            throw new RuntimeException("Failed to generate bill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get bill by visit ID
     * GET /visits/{visitId}/bill/details
     */
    @GetMapping("/visits/{visitId}/bill/details")
    public ResponseEntity<BillingResponse> getBillByVisitId(@PathVariable Long visitId) {
        try {
            BillingResponse response = billingService.getBillByVisitId(visitId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to get bill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get bill by bill ID
     * GET /billing/{billId}
     */
    @GetMapping("/{billId}")
    public ResponseEntity<BillingResponse> getBill(@PathVariable Long billId) {
        try {
            BillingResponse response = billingService.getBill(billId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to get bill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all bills
     * GET /billing
     */
    @GetMapping
    public ResponseEntity<List<BillingResponse>> getAllBills() {
        try {
            List<BillingResponse> bills = billingService.getAllBills();
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bills: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get unpaid bills
     * GET /billing/unpaid
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<BillingResponse>> getUnpaidBills() {
        try {
            List<BillingResponse> bills = billingService.getUnpaidBills();
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get unpaid bills: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark bill as paid
     * PATCH /billing/{billId}/pay
     */
    @PatchMapping("/{billId}/pay")
    public ResponseEntity<BillingResponse> markBillAsPaid(@PathVariable Long billId) {
        try {
            BillingResponse response = billingService.markBillAsPaid(billId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("already")) {
                return ResponseEntity.badRequest().build();
            }
            throw new RuntimeException("Failed to mark bill as paid: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get revenue statistics
     * GET /billing/stats
     */
    @GetMapping("/stats")
    @Operation(summary = "Get revenue statistics", description = "Retrieve revenue statistics")
    public ResponseEntity<BillingService.RevenueStats> getRevenueStats() {
        try {
            BillingService.RevenueStats stats = billingService.getRevenueStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting revenue stats", e);
            throw new RuntimeException("Failed to get revenue stats: " + e.getMessage(), e);
        }
    }

    /**
     * Get billing statistics
     * GET /billing/statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get billing statistics", description = "Retrieve comprehensive billing statistics")
    public ResponseEntity<Map<String, Object>> getBillingStatistics() {
        try {
            Map<String, Object> statistics = billingService.getBillingStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting billing statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve billing statistics", "message", e.getMessage()));
        }
    }
    
    /**
     * Get revenue for period
     * GET /billing/revenue?startDate=2023-01-01T00:00:00&endDate=2023-12-31T23:59:59
     */
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenueForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            BigDecimal revenue = billingService.getRevenueForPeriod(startDate, endDate);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get revenue for period: " + e.getMessage(), e);
        }
    }
}
