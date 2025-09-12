package com.sivalab.laboperations.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AddTestToVisitRequest {
    
    @NotNull(message = "Test template ID is required")
    private Long testTemplateId;
    
    private BigDecimal price; // Optional, will use base price if not provided
    
    // Constructors
    public AddTestToVisitRequest() {}
    
    public AddTestToVisitRequest(Long testTemplateId, BigDecimal price) {
        this.testTemplateId = testTemplateId;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getTestTemplateId() {
        return testTemplateId;
    }
    
    public void setTestTemplateId(Long testTemplateId) {
        this.testTemplateId = testTemplateId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
