package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public class CreateVisitRequest {
    
    @NotNull(message = "Patient details are required")
    private JsonNode patientDetails;
    
    // Constructors
    public CreateVisitRequest() {}
    
    public CreateVisitRequest(JsonNode patientDetails) {
        this.patientDetails = patientDetails;
    }
    
    // Getters and Setters
    public JsonNode getPatientDetails() {
        return patientDetails;
    }
    
    public void setPatientDetails(JsonNode patientDetails) {
        this.patientDetails = patientDetails;
    }
}
