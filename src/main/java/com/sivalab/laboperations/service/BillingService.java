package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.BillingResponse;
import com.sivalab.laboperations.entity.Billing;
import com.sivalab.laboperations.entity.Visit;
import com.sivalab.laboperations.entity.VisitStatus;
import com.sivalab.laboperations.repository.BillingRepository;
import com.sivalab.laboperations.repository.LabTestRepository;
import com.sivalab.laboperations.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillingService {
    
    private final BillingRepository billingRepository;
    private final VisitRepository visitRepository;
    private final LabTestRepository labTestRepository;
    
    @Autowired
    public BillingService(BillingRepository billingRepository, 
                         VisitRepository visitRepository,
                         LabTestRepository labTestRepository) {
        this.billingRepository = billingRepository;
        this.visitRepository = visitRepository;
        this.labTestRepository = labTestRepository;
    }
    
    /**
     * Generate bill for visit
     */
    public BillingResponse generateBill(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));
        
        // Check if visit is approved
        if (visit.getStatus() != VisitStatus.APPROVED) {
            throw new RuntimeException("Cannot generate bill for visit that is not approved");
        }
        
        // Check if bill already exists
        if (billingRepository.findByVisitVisitId(visitId).isPresent()) {
            throw new RuntimeException("Bill already exists for visit ID: " + visitId);
        }
        
        // Calculate total amount from lab tests
        BigDecimal totalAmount = labTestRepository.calculateTotalPriceForVisit(visitId);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Cannot generate bill with zero or negative amount");
        }
        
        Billing billing = new Billing(visit, totalAmount);
        billing = billingRepository.save(billing);
        
        // Update visit status to billed
        visit.setStatus(VisitStatus.BILLED);
        visitRepository.save(visit);
        
        return convertToResponse(billing);
    }
    
    /**
     * Get bill by visit ID
     */
    @Transactional(readOnly = true)
    public BillingResponse getBillByVisitId(Long visitId) {
        Billing billing = billingRepository.findByVisitVisitId(visitId)
                .orElseThrow(() -> new RuntimeException("Bill not found for visit ID: " + visitId));
        return convertToResponse(billing);
    }
    
    /**
     * Get bill by bill ID
     */
    @Transactional(readOnly = true)
    public BillingResponse getBill(Long billId) {
        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        return convertToResponse(billing);
    }
    
    /**
     * Get all bills
     */
    @Transactional(readOnly = true)
    public List<BillingResponse> getAllBills() {
        return billingRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unpaid bills
     */
    @Transactional(readOnly = true)
    public List<BillingResponse> getUnpaidBills() {
        return billingRepository.findByPaid(false).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark bill as paid
     */
    public BillingResponse markBillAsPaid(Long billId) {
        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        
        if (billing.getPaid()) {
            throw new RuntimeException("Bill is already marked as paid");
        }
        
        billing.setPaid(true);
        billing = billingRepository.save(billing);
        
        // Update visit status to completed
        Visit visit = billing.getVisit();
        visit.setStatus(VisitStatus.COMPLETED);
        visitRepository.save(visit);
        
        return convertToResponse(billing);
    }
    
    /**
     * Get revenue statistics
     */
    @Transactional(readOnly = true)
    public RevenueStats getRevenueStats() {
        BigDecimal totalRevenue = billingRepository.calculateTotalRevenue();
        BigDecimal outstandingAmount = billingRepository.calculateOutstandingAmount();
        long paidBillsCount = billingRepository.countByPaid(true);
        long unpaidBillsCount = billingRepository.countByPaid(false);
        
        return new RevenueStats(totalRevenue, outstandingAmount, paidBillsCount, unpaidBillsCount);
    }
    
    /**
     * Get revenue for period
     */
    @Transactional(readOnly = true)
    public BigDecimal getRevenueForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return billingRepository.calculateRevenueForPeriod(startDate, endDate);
    }
    
    /**
     * Convert Billing entity to BillingResponse DTO
     */
    public BillingResponse convertToResponse(Billing billing) {
        return new BillingResponse(
                billing.getBillId(),
                billing.getVisit().getVisitId(),
                billing.getTotalAmount(),
                billing.getPaid(),
                billing.getCreatedAt()
        );
    }
    
    /**
     * Revenue statistics DTO
     */
    public static class RevenueStats {
        private final BigDecimal totalRevenue;
        private final BigDecimal outstandingAmount;
        private final long paidBillsCount;
        private final long unpaidBillsCount;
        
        public RevenueStats(BigDecimal totalRevenue, BigDecimal outstandingAmount, 
                           long paidBillsCount, long unpaidBillsCount) {
            this.totalRevenue = totalRevenue;
            this.outstandingAmount = outstandingAmount;
            this.paidBillsCount = paidBillsCount;
            this.unpaidBillsCount = unpaidBillsCount;
        }
        
        // Getters
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getOutstandingAmount() { return outstandingAmount; }
        public long getPaidBillsCount() { return paidBillsCount; }
        public long getUnpaidBillsCount() { return unpaidBillsCount; }
    }
}
