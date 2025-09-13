package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTestTemplateRequest {
    
    @NotBlank(message = "Test name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Parameters are required")
    private JsonNode parameters;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Base price must be greater than 0")
    private BigDecimal basePrice;
    
    // Constructors
    public CreateTestTemplateRequest() {}
    
    public CreateTestTemplateRequest(String name, String description, JsonNode parameters, BigDecimal basePrice) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.basePrice = basePrice;
    }
    
    // Getters and Setters
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
}
