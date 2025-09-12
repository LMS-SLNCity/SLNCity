package com.sivalab.laboperations.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillingResponse {
    
    private Long billId;
    private Long visitId;
    private BigDecimal totalAmount;
    private Boolean paid;
    private LocalDateTime createdAt;
    
    // Constructors
    public BillingResponse() {}
    
    public BillingResponse(Long billId, Long visitId, BigDecimal totalAmount, Boolean paid, LocalDateTime createdAt) {
        this.billId = billId;
        this.visitId = visitId;
        this.totalAmount = totalAmount;
        this.paid = paid;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getBillId() {
        return billId;
    }
    
    public void setBillId(Long billId) {
        this.billId = billId;
    }
    
    public Long getVisitId() {
        return visitId;
    }
    
    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Boolean getPaid() {
        return paid;
    }
    
    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
