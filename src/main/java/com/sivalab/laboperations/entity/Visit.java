package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visits")
public class Visit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patient_details", nullable = false, columnDefinition = "json")
    private JsonNode patientDetails;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VisitStatus status = VisitStatus.PENDING;
    
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"visit", "sample", "hibernateLazyInitializer", "handler"})
    private List<LabTest> labTests = new ArrayList<>();
    
    @OneToOne(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Billing billing;
    
    // Constructors
    public Visit() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Visit(JsonNode patientDetails) {
        this();
        this.patientDetails = patientDetails;
    }
    
    // Getters and Setters
    public Long getVisitId() {
        return visitId;
    }
    
    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
    
    public JsonNode getPatientDetails() {
        return patientDetails;
    }
    
    public void setPatientDetails(JsonNode patientDetails) {
        this.patientDetails = patientDetails;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public VisitStatus getStatus() {
        return status;
    }
    
    public void setStatus(VisitStatus status) {
        this.status = status;
    }
    
    public List<LabTest> getLabTests() {
        return labTests;
    }
    
    public void setLabTests(List<LabTest> labTests) {
        this.labTests = labTests;
    }
    
    public Billing getBilling() {
        return billing;
    }
    
    public void setBilling(Billing billing) {
        this.billing = billing;
    }
    
    // Helper methods
    public void addLabTest(LabTest labTest) {
        labTests.add(labTest);
        labTest.setVisit(this);
    }
    
    public void removeLabTest(LabTest labTest) {
        labTests.remove(labTest);
        labTest.setVisit(null);
    }
}
