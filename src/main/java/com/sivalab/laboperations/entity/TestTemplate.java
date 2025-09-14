package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_templates")
public class TestTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", nullable = false, columnDefinition = "json")
    private JsonNode parameters;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "testTemplate", fetch = FetchType.LAZY)
    private List<LabTest> labTests = new ArrayList<>();
    
    // Constructors
    public TestTemplate() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TestTemplate(String name, String description, JsonNode parameters, BigDecimal basePrice, LocalDateTime createdAt) {
        this();
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.basePrice = basePrice;
        this.createdAt = LocalDateTime.now();
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
    
    public List<LabTest> getLabTests() {
        return labTests;
    }
    
    public void setLabTests(List<LabTest> labTests) {
        this.labTests = labTests;
    }
}
