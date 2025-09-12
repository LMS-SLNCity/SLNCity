package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestTemplateResponse {
    
    private Long templateId;
    private String name;
    private String description;
    private JsonNode parameters;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;
    
    // Constructors
    public TestTemplateResponse() {}
    
    public TestTemplateResponse(Long templateId, String name, String description, 
                               JsonNode parameters, BigDecimal basePrice, LocalDateTime createdAt) {
        this.templateId = templateId;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public JsonNode getParameters() {
        return parameters;
    }
    
    public void setParameters(JsonNode parameters) {
        this.parameters = parameters;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
