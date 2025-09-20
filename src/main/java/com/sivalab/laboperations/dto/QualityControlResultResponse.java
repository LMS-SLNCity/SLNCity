package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class QualityControlResultResponse {
    private Long id;
    private JsonNode results;
    private boolean passed;
    private LocalDateTime testedAt;
    private Long qualityControlId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getTestedAt() {
        return testedAt;
    }

    public void setTestedAt(LocalDateTime testedAt) {
        this.testedAt = testedAt;
    }

    public Long getQualityControlId() {
        return qualityControlId;
    }

    public void setQualityControlId(Long qualityControlId) {
        this.qualityControlId = qualityControlId;
    }
}
