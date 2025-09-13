package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing laboratory equipment
 */
@Entity
@Table(name = "lab_equipment")
public class LabEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Model is required")
    @Column(name = "model", nullable = false)
    private String model;

    @NotBlank(message = "Manufacturer is required")
    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @NotBlank(message = "Serial number is required")
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "location")
    private String location;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @Column(name = "next_maintenance")
    private LocalDateTime nextMaintenance;

    @Column(name = "calibration_due")
    private LocalDateTime calibrationDue;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquipmentMaintenance> maintenanceHistory = new ArrayList<>();

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquipmentCalibration> calibrationHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public LabEquipment() {}

    public LabEquipment(String name, String model, String manufacturer, String serialNumber, EquipmentType equipmentType) {
        this.name = name;
        this.model = model;
        this.manufacturer = manufacturer;
        this.serialNumber = serialNumber;
        this.equipmentType = equipmentType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public EquipmentType getEquipmentType() { return equipmentType; }
    public void setEquipmentType(EquipmentType equipmentType) { this.equipmentType = equipmentType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDateTime getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(LocalDateTime warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }

    public LocalDateTime getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(LocalDateTime lastMaintenance) { this.lastMaintenance = lastMaintenance; }

    public LocalDateTime getNextMaintenance() { return nextMaintenance; }
    public void setNextMaintenance(LocalDateTime nextMaintenance) { this.nextMaintenance = nextMaintenance; }

    public LocalDateTime getCalibrationDue() { return calibrationDue; }
    public void setCalibrationDue(LocalDateTime calibrationDue) { this.calibrationDue = calibrationDue; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<EquipmentMaintenance> getMaintenanceHistory() { return maintenanceHistory; }
    public void setMaintenanceHistory(List<EquipmentMaintenance> maintenanceHistory) { this.maintenanceHistory = maintenanceHistory; }

    public List<EquipmentCalibration> getCalibrationHistory() { return calibrationHistory; }
    public void setCalibrationHistory(List<EquipmentCalibration> calibrationHistory) { this.calibrationHistory = calibrationHistory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isMaintenanceDue() {
        return nextMaintenance != null && nextMaintenance.isBefore(LocalDateTime.now());
    }

    public boolean isCalibrationDue() {
        return calibrationDue != null && calibrationDue.isBefore(LocalDateTime.now());
    }

    public boolean isWarrantyExpired() {
        return warrantyExpiry != null && warrantyExpiry.isBefore(LocalDateTime.now());
    }
}
