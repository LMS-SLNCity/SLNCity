package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ulr_sequence_config")
public class UlrSequenceConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;
    
    @Column(name = "report_year", nullable = false)
    private Integer year;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 1;
    
    @Column(name = "prefix", nullable = false, length = 10)
    private String prefix = "SLN";
    
    @Column(name = "format_pattern", nullable = false, length = 50)
    private String formatPattern = "{prefix}/{year}/{sequence:06d}";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UlrSequenceConfig() {}
    
    public UlrSequenceConfig(Integer year, String prefix) {
        this.year = year;
        this.prefix = prefix;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getConfigId() {
        return configId;
    }
    
    public void setConfigId(Long configId) {
        this.configId = configId;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getFormatPattern() {
        return formatPattern;
    }
    
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
