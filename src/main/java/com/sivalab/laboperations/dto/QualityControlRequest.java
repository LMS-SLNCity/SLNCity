package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QualityControlRequest {

    @NotNull
    private Long testTemplateId;

    @NotBlank
    private String controlName;

    @NotNull
    private Integer controlLevel;

    @NotNull
    private JsonNode frequency;

    @NotBlank
    private String westgardRules;

    public Long getTestTemplateId() {
        return testTemplateId;
    }

    public void setTestTemplateId(Long testTemplateId) {
        this.testTemplateId = testTemplateId;
    }

    public String getControlName() {
        return controlName;
    }

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public Integer getControlLevel() {
        return controlLevel;
    }

    public void setControlLevel(Integer controlLevel) {
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
}
