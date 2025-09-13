package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing equipment calibration records
 */
@Entity
@Table(name = "equipment_calibration")
public class EquipmentCalibration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    @NotNull(message = "Equipment is required")
    private LabEquipment equipment;

    @Column(name = "calibration_date", nullable = false)
    private LocalDateTime calibrationDate;

    @Column(name = "next_calibration_due")
    private LocalDateTime nextCalibrationDue;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "calibration_standard")
    private String calibrationStandard;

    @Column(name = "reference_material")
    private String referenceMaterial;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "humidity")
    private BigDecimal humidity;

    @Enumerated(EnumType.STRING)
    @Column(name = "calibration_result", nullable = false)
    private CalibrationResult calibrationResult;

    @Column(name = "accuracy_achieved")
    private BigDecimal accuracyAchieved;

    @Column(name = "tolerance_limit")
    private BigDecimal toleranceLimit;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "calibration_agency")
    private String calibrationAgency;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public EquipmentCalibration() {}

    public EquipmentCalibration(LabEquipment equipment, LocalDateTime calibrationDate, CalibrationResult calibrationResult) {
        this.equipment = equipment;
        this.calibrationDate = calibrationDate;
        this.calibrationResult = calibrationResult;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LabEquipment getEquipment() { return equipment; }
    public void setEquipment(LabEquipment equipment) { this.equipment = equipment; }

    public LocalDateTime getCalibrationDate() { return calibrationDate; }
    public void setCalibrationDate(LocalDateTime calibrationDate) { this.calibrationDate = calibrationDate; }

    public LocalDateTime getNextCalibrationDue() { return nextCalibrationDue; }
    public void setNextCalibrationDue(LocalDateTime nextCalibrationDue) { this.nextCalibrationDue = nextCalibrationDue; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getCalibrationStandard() { return calibrationStandard; }
    public void setCalibrationStandard(String calibrationStandard) { this.calibrationStandard = calibrationStandard; }

    public String getReferenceMaterial() { return referenceMaterial; }
    public void setReferenceMaterial(String referenceMaterial) { this.referenceMaterial = referenceMaterial; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }

    public CalibrationResult getCalibrationResult() { return calibrationResult; }
    public void setCalibrationResult(CalibrationResult calibrationResult) { this.calibrationResult = calibrationResult; }

    public BigDecimal getAccuracyAchieved() { return accuracyAchieved; }
    public void setAccuracyAchieved(BigDecimal accuracyAchieved) { this.accuracyAchieved = accuracyAchieved; }

    public BigDecimal getToleranceLimit() { return toleranceLimit; }
    public void setToleranceLimit(BigDecimal toleranceLimit) { this.toleranceLimit = toleranceLimit; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public String getCalibrationAgency() { return calibrationAgency; }
    public void setCalibrationAgency(String calibrationAgency) { this.calibrationAgency = calibrationAgency; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Enumeration for calibration results
     */
    public enum CalibrationResult {
        PASSED("Passed", "Equipment meets calibration standards"),
        FAILED("Failed", "Equipment does not meet calibration standards"),
        ADJUSTED("Adjusted", "Equipment was adjusted to meet standards"),
        LIMITED_USE("Limited Use", "Equipment has limited accuracy but usable"),
        OUT_OF_TOLERANCE("Out of Tolerance", "Equipment is outside acceptable limits");

        private final String displayName;
        private final String description;

        CalibrationResult(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
