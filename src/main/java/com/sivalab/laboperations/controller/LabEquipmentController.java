package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.EquipmentStatus;
import com.sivalab.laboperations.entity.EquipmentType;
import com.sivalab.laboperations.entity.LabEquipment;
import com.sivalab.laboperations.service.LabEquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing laboratory equipment
 */
@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Lab Equipment", description = "Laboratory equipment management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LabEquipmentController {

    @Autowired
    private LabEquipmentService equipmentService;

    /**
     * Create new equipment
     */
    @PostMapping
    @Operation(summary = "Create new equipment", description = "Add new laboratory equipment to the system")
    public ResponseEntity<LabEquipment> createEquipment(@Valid @RequestBody LabEquipment equipment) {
        LabEquipment createdEquipment = equipmentService.createEquipment(equipment);
        return new ResponseEntity<>(createdEquipment, HttpStatus.CREATED);
    }

    /**
     * Get equipment by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID", description = "Retrieve equipment details by ID")
    public ResponseEntity<LabEquipment> getEquipmentById(
            @Parameter(description = "Equipment ID") @PathVariable Long id) {
        return equipmentService.getEquipmentById(id)
                .map(equipment -> ResponseEntity.ok(equipment))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get equipment by serial number
     */
    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Get equipment by serial number", description = "Retrieve equipment details by serial number")
    public ResponseEntity<LabEquipment> getEquipmentBySerialNumber(
            @Parameter(description = "Equipment serial number") @PathVariable String serialNumber) {
        return equipmentService.getEquipmentBySerialNumber(serialNumber)
                .map(equipment -> ResponseEntity.ok(equipment))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all equipment
     */
    @GetMapping
    @Operation(summary = "Get all equipment", description = "Retrieve all laboratory equipment")
    public ResponseEntity<List<LabEquipment>> getAllEquipment() {
        List<LabEquipment> equipment = equipmentService.getAllEquipment();
        return ResponseEntity.ok(equipment);
    }

    /**
     * Get equipment by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get equipment by status", description = "Retrieve equipment filtered by status")
    public ResponseEntity<List<LabEquipment>> getEquipmentByStatus(
            @Parameter(description = "Equipment status") @PathVariable EquipmentStatus status) {
        List<LabEquipment> equipment = equipmentService.getEquipmentByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Get equipment by type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get equipment by type", description = "Retrieve equipment filtered by type")
    public ResponseEntity<List<LabEquipment>> getEquipmentByType(
            @Parameter(description = "Equipment type") @PathVariable EquipmentType type) {
        List<LabEquipment> equipment = equipmentService.getEquipmentByType(type);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Search equipment with pagination
     */
    @GetMapping("/search")
    @Operation(summary = "Search equipment", description = "Search equipment with multiple criteria and pagination")
    public ResponseEntity<Page<LabEquipment>> searchEquipment(
            @Parameter(description = "Equipment name") @RequestParam(required = false) String name,
            @Parameter(description = "Manufacturer") @RequestParam(required = false) String manufacturer,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Status") @RequestParam(required = false) EquipmentStatus status,
            @Parameter(description = "Equipment type") @RequestParam(required = false) EquipmentType equipmentType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<LabEquipment> equipment = equipmentService.searchEquipment(
                name, manufacturer, location, status, equipmentType, pageable);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Update equipment
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update equipment", description = "Update existing equipment details")
    public ResponseEntity<LabEquipment> updateEquipment(
            @Parameter(description = "Equipment ID") @PathVariable Long id,
            @Valid @RequestBody LabEquipment equipment) {
        try {
            LabEquipment updatedEquipment = equipmentService.updateEquipment(id, equipment);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update equipment status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update equipment status", description = "Update equipment status")
    public ResponseEntity<LabEquipment> updateEquipmentStatus(
            @Parameter(description = "Equipment ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam EquipmentStatus status) {
        try {
            LabEquipment updatedEquipment = equipmentService.updateEquipmentStatus(id, status);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete equipment
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment", description = "Remove equipment from the system")
    public ResponseEntity<Void> deleteEquipment(
            @Parameter(description = "Equipment ID") @PathVariable Long id) {
        try {
            equipmentService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get equipment requiring maintenance
     */
    @GetMapping("/maintenance-due")
    @Operation(summary = "Get equipment requiring maintenance", description = "Retrieve equipment that requires maintenance")
    public ResponseEntity<List<LabEquipment>> getEquipmentWithMaintenanceDue() {
        List<LabEquipment> equipment = equipmentService.getEquipmentWithMaintenanceDue();
        return ResponseEntity.ok(equipment);
    }

    /**
     * Get equipment requiring calibration
     */
    @GetMapping("/calibration-due")
    @Operation(summary = "Get equipment requiring calibration", description = "Retrieve equipment that requires calibration")
    public ResponseEntity<List<LabEquipment>> getEquipmentWithCalibrationDue() {
        List<LabEquipment> equipment = equipmentService.getEquipmentWithCalibrationDue();
        return ResponseEntity.ok(equipment);
    }

    /**
     * Schedule maintenance
     */
    @PostMapping("/{id}/schedule-maintenance")
    @Operation(summary = "Schedule maintenance", description = "Schedule maintenance for equipment")
    public ResponseEntity<LabEquipment> scheduleMaintenance(
            @Parameter(description = "Equipment ID") @PathVariable Long id,
            @Parameter(description = "Maintenance date") @RequestParam String maintenanceDate) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(maintenanceDate);
            LabEquipment updatedEquipment = equipmentService.scheduleMaintenance(id, dateTime);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete maintenance
     */
    @PostMapping("/{id}/complete-maintenance")
    @Operation(summary = "Complete maintenance", description = "Mark maintenance as completed for equipment")
    public ResponseEntity<LabEquipment> completeMaintenance(
            @Parameter(description = "Equipment ID") @PathVariable Long id,
            @Parameter(description = "Next maintenance date") @RequestParam(required = false) String nextMaintenanceDate) {
        try {
            LocalDateTime nextDate = nextMaintenanceDate != null ? LocalDateTime.parse(nextMaintenanceDate) : null;
            LabEquipment updatedEquipment = equipmentService.completeMaintenance(id, nextDate);
            return ResponseEntity.ok(updatedEquipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get equipment statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get equipment statistics", description = "Retrieve equipment statistics and metrics")
    public ResponseEntity<Map<String, Object>> getEquipmentStatistics() {
        Map<String, Object> statistics = equipmentService.getEquipmentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get equipment types
     */
    @GetMapping("/types")
    @Operation(summary = "Get equipment types", description = "Retrieve all available equipment types")
    public ResponseEntity<EquipmentType[]> getEquipmentTypes() {
        return ResponseEntity.ok(EquipmentType.values());
    }

    /**
     * Get equipment statuses
     */
    @GetMapping("/statuses")
    @Operation(summary = "Get equipment statuses", description = "Retrieve all available equipment statuses")
    public ResponseEntity<EquipmentStatus[]> getEquipmentStatuses() {
        return ResponseEntity.ok(EquipmentStatus.values());
    }
}
