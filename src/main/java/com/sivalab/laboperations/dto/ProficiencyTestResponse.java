package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;

public class ProficiencyTestResponse {
    private Long id;
    private String provider;
    private LocalDate testDate;
    private Long testTemplateId;
    private JsonNode results;
    private boolean passed;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public Long getTestTemplateId() {
        return testTemplateId;
    }

    public void setTestTemplateId(Long testTemplateId) {
        this.testTemplateId = testTemplateId;
    }

    public JsonNode getResults() {
        return results;
    }

    public void setResults(JsonNode results) {
        this.results = results;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
