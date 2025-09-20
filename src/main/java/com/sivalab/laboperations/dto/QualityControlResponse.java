package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class QualityControlResponse {
    private Long id;
    private String controlName;
    private int controlLevel;
    private JsonNode frequency;
    private String westgardRules;
    private LocalDateTime nextDueDate;
    private Long testTemplateId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getControlName() {
        return controlName;
    }

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public int getControlLevel() {
        return controlLevel;
    }

    public void setControlLevel(int controlLevel) {
        this.controlLevel = controlLevel;
    }

    public JsonNode getFrequency() {
        return frequency;
    }

    public void setFrequency(JsonNode frequency) {
        this.frequency = frequency;
    }

    public String getWestgardRules() {
        return westgardRules;
    }

    public void setWestgardRules(String westgardRules) {
        this.westgardRules = westgardRules;
    }

    public LocalDateTime getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDateTime nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public Long getTestTemplateId() {
        return testTemplateId;
    }

    public void setTestTemplateId(Long testTemplateId) {
        this.testTemplateId = testTemplateId;
    }
}
