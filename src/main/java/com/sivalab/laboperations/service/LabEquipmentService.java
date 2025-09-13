package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.LabEquipmentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing laboratory equipment
 */
@Service
@Transactional
public class LabEquipmentService {

    private static final Logger logger = LoggerFactory.getLogger(LabEquipmentService.class);

    @Autowired
    private LabEquipmentRepository equipmentRepository;

    /**
     * Create new equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCreateEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public LabEquipment createEquipment(LabEquipment equipment) {
        logger.info("Creating new equipment: {}", equipment.getName());
        
        // Check if serial number already exists
        if (equipmentRepository.findBySerialNumber(equipment.getSerialNumber()).isPresent()) {
            throw new IllegalArgumentException("Equipment with serial number " + equipment.getSerialNumber() + " already exists");
        }
        
        return equipmentRepository.save(equipment);
    }

    /**
     * Get equipment by ID
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<LabEquipment> getEquipmentById(Long id) {
        logger.debug("Fetching equipment with ID: {}", id);
        return equipmentRepository.findById(id);
    }

    /**
     * Get equipment by serial number
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetEquipmentBySerial")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Optional<LabEquipment> getEquipmentBySerialNumber(String serialNumber) {
        logger.debug("Fetching equipment with serial number: {}", serialNumber);
        return equipmentRepository.findBySerialNumber(serialNumber);
    }

    /**
     * Get all equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetAllEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<LabEquipment> getAllEquipment() {
        logger.debug("Fetching all equipment");
        return equipmentRepository.findAll();
    }

    /**
     * Get equipment by status
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetEquipmentByStatus")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<LabEquipment> getEquipmentByStatus(EquipmentStatus status) {
        logger.debug("Fetching equipment with status: {}", status);
        return equipmentRepository.findByStatus(status);
    }

    /**
     * Get equipment by type
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetEquipmentByType")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<LabEquipment> getEquipmentByType(EquipmentType equipmentType) {
        logger.debug("Fetching equipment with type: {}", equipmentType);
        return equipmentRepository.findByEquipmentType(equipmentType);
    }

    /**
     * Search equipment with pagination
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackSearchEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Page<LabEquipment> searchEquipment(String name, String manufacturer, String location, 
                                            EquipmentStatus status, EquipmentType equipmentType, 
                                            Pageable pageable) {
        logger.debug("Searching equipment with criteria - name: {}, manufacturer: {}, location: {}, status: {}, type: {}", 
                    name, manufacturer, location, status, equipmentType);
        return equipmentRepository.searchEquipment(name, manufacturer, location, status, equipmentType, pageable);
    }

    /**
     * Update equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public LabEquipment updateEquipment(Long id, LabEquipment updatedEquipment) {
        logger.info("Updating equipment with ID: {}", id);
        
        return equipmentRepository.findById(id)
                .map(equipment -> {
                    equipment.setName(updatedEquipment.getName());
                    equipment.setModel(updatedEquipment.getModel());
                    equipment.setManufacturer(updatedEquipment.getManufacturer());
                    equipment.setStatus(updatedEquipment.getStatus());
                    equipment.setLocation(updatedEquipment.getLocation());
                    equipment.setNotes(updatedEquipment.getNotes());
                    equipment.setNextMaintenance(updatedEquipment.getNextMaintenance());
                    equipment.setCalibrationDue(updatedEquipment.getCalibrationDue());
                    return equipmentRepository.save(equipment);
                })
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + id));
    }

    /**
     * Update equipment status
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackUpdateEquipmentStatus")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public LabEquipment updateEquipmentStatus(Long id, EquipmentStatus status) {
        logger.info("Updating equipment status for ID: {} to {}", id, status);
        
        return equipmentRepository.findById(id)
                .map(equipment -> {
                    equipment.setStatus(status);
                    return equipmentRepository.save(equipment);
                })
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + id));
    }

    /**
     * Delete equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackDeleteEquipment")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public void deleteEquipment(Long id) {
        logger.info("Deleting equipment with ID: {}", id);
        
        if (!equipmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Equipment not found with ID: " + id);
        }
        
        equipmentRepository.deleteById(id);
    }

    /**
     * Get equipment requiring maintenance
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetMaintenanceDue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<LabEquipment> getEquipmentWithMaintenanceDue() {
        logger.debug("Fetching equipment with maintenance due");
        return equipmentRepository.findEquipmentWithMaintenanceDue(LocalDateTime.now());
    }

    /**
     * Get equipment requiring calibration
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetCalibrationDue")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public List<LabEquipment> getEquipmentWithCalibrationDue() {
        logger.debug("Fetching equipment with calibration due");
        return equipmentRepository.findEquipmentWithCalibrationDue(LocalDateTime.now());
    }

    /**
     * Get equipment statistics
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackGetEquipmentStatistics")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public Map<String, Object> getEquipmentStatistics() {
        logger.debug("Generating equipment statistics");
        
        List<Object[]> statusStats = equipmentRepository.getEquipmentStatusStatistics();
        List<Object[]> typeStats = equipmentRepository.getEquipmentTypeStatistics();
        
        Map<EquipmentStatus, Long> statusMap = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (EquipmentStatus) row[0],
                        row -> (Long) row[1]
                ));
        
        Map<EquipmentType, Long> typeMap = typeStats.stream()
                .collect(Collectors.toMap(
                        row -> (EquipmentType) row[0],
                        row -> (Long) row[1]
                ));
        
        return Map.of(
                "statusStatistics", statusMap,
                "typeStatistics", typeMap,
                "totalEquipment", equipmentRepository.count(),
                "maintenanceDue", equipmentRepository.findEquipmentWithMaintenanceDue(LocalDateTime.now()).size(),
                "calibrationDue", equipmentRepository.findEquipmentWithCalibrationDue(LocalDateTime.now()).size()
        );
    }

    // Fallback methods
    public LabEquipment fallbackCreateEquipment(LabEquipment equipment, Exception ex) {
        logger.error("Fallback: Failed to create equipment", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }

    public Optional<LabEquipment> fallbackGetEquipment(Long id, Exception ex) {
        logger.error("Fallback: Failed to get equipment by ID", ex);
        return Optional.empty();
    }

    public Optional<LabEquipment> fallbackGetEquipmentBySerial(String serialNumber, Exception ex) {
        logger.error("Fallback: Failed to get equipment by serial number", ex);
        return Optional.empty();
    }

    public List<LabEquipment> fallbackGetAllEquipment(Exception ex) {
        logger.error("Fallback: Failed to get all equipment", ex);
        return List.of();
    }

    public List<LabEquipment> fallbackGetEquipmentByStatus(EquipmentStatus status, Exception ex) {
        logger.error("Fallback: Failed to get equipment by status", ex);
        return List.of();
    }

    public List<LabEquipment> fallbackGetEquipmentByType(EquipmentType equipmentType, Exception ex) {
        logger.error("Fallback: Failed to get equipment by type", ex);
        return List.of();
    }

    public Page<LabEquipment> fallbackSearchEquipment(String name, String manufacturer, String location, 
                                                    EquipmentStatus status, EquipmentType equipmentType, 
                                                    Pageable pageable, Exception ex) {
        logger.error("Fallback: Failed to search equipment", ex);
        return Page.empty();
    }

    public LabEquipment fallbackUpdateEquipment(Long id, LabEquipment updatedEquipment, Exception ex) {
        logger.error("Fallback: Failed to update equipment", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }

    public LabEquipment fallbackUpdateEquipmentStatus(Long id, EquipmentStatus status, Exception ex) {
        logger.error("Fallback: Failed to update equipment status", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }

    public void fallbackDeleteEquipment(Long id, Exception ex) {
        logger.error("Fallback: Failed to delete equipment", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }

    public List<LabEquipment> fallbackGetMaintenanceDue(Exception ex) {
        logger.error("Fallback: Failed to get maintenance due equipment", ex);
        return List.of();
    }

    public List<LabEquipment> fallbackGetCalibrationDue(Exception ex) {
        logger.error("Fallback: Failed to get calibration due equipment", ex);
        return List.of();
    }

    public Map<String, Object> fallbackGetEquipmentStatistics(Exception ex) {
        logger.error("Fallback: Failed to get equipment statistics", ex);
        return Map.of("error", "Statistics temporarily unavailable");
    }

    /**
     * Schedule maintenance for equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackScheduleMaintenance")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public LabEquipment scheduleMaintenance(Long equipmentId, LocalDateTime maintenanceDate) {
        logger.info("Scheduling maintenance for equipment ID: {} on {}", equipmentId, maintenanceDate);

        return equipmentRepository.findById(equipmentId)
                .map(equipment -> {
                    equipment.setNextMaintenance(maintenanceDate);
                    equipment.setStatus(EquipmentStatus.MAINTENANCE);
                    return equipmentRepository.save(equipment);
                })
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipmentId));
    }

    /**
     * Complete maintenance for equipment
     */
    @CircuitBreaker(name = "database", fallbackMethod = "fallbackCompleteMaintenance")
    @RateLimiter(name = "api")
    @Retry(name = "database")
    public LabEquipment completeMaintenance(Long equipmentId, LocalDateTime nextMaintenanceDate) {
        logger.info("Completing maintenance for equipment ID: {}", equipmentId);

        return equipmentRepository.findById(equipmentId)
                .map(equipment -> {
                    equipment.setLastMaintenance(LocalDateTime.now());
                    equipment.setNextMaintenance(nextMaintenanceDate);
                    equipment.setStatus(EquipmentStatus.ACTIVE);
                    return equipmentRepository.save(equipment);
                })
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipmentId));
    }

    public LabEquipment fallbackScheduleMaintenance(Long equipmentId, LocalDateTime maintenanceDate, Exception ex) {
        logger.error("Fallback: Failed to schedule maintenance", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }

    public LabEquipment fallbackCompleteMaintenance(Long equipmentId, LocalDateTime nextMaintenanceDate, Exception ex) {
        logger.error("Fallback: Failed to complete maintenance", ex);
        throw new RuntimeException("Equipment service temporarily unavailable");
    }
}
