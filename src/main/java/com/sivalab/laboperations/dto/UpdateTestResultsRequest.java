package com.sivalab.laboperations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public class UpdateTestResultsRequest {
    
    @NotNull(message = "Results are required")
    private JsonNode results;
    
    // Constructors
    public UpdateTestResultsRequest() {}
    
    public UpdateTestResultsRequest(JsonNode results) {
        this.results = results;
    }
    
    // Getters and Setters
    public JsonNode getResults() {
        return results;
    }
    
    public void setResults(JsonNode results) {
        this.results = results;
    }
}
