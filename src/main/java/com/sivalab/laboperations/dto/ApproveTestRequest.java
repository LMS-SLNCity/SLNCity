package com.sivalab.laboperations.dto;

import jakarta.validation.constraints.NotBlank;

public class ApproveTestRequest {
    
    @NotBlank(message = "Approver name is required")
    private String approvedBy;
    
    // Constructors
    public ApproveTestRequest() {}
    
    public ApproveTestRequest(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    // Getters and Setters
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
