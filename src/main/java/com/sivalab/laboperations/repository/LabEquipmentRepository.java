package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.EquipmentStatus;
import com.sivalab.laboperations.entity.EquipmentType;
import com.sivalab.laboperations.entity.LabEquipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LabEquipment entity
 */
@Repository
public interface LabEquipmentRepository extends JpaRepository<LabEquipment, Long> {

    /**
     * Find equipment by serial number
     */
    Optional<LabEquipment> findBySerialNumber(String serialNumber);

    /**
     * Find equipment by status
     */
    List<LabEquipment> findByStatus(EquipmentStatus status);

    /**
     * Find equipment by type
     */
    List<LabEquipment> findByEquipmentType(EquipmentType equipmentType);

    /**
     * Find equipment by location
     */
    List<LabEquipment> findByLocationContainingIgnoreCase(String location);

    /**
     * Find equipment by manufacturer
     */
    List<LabEquipment> findByManufacturerContainingIgnoreCase(String manufacturer);

    /**
     * Find equipment by name containing (case insensitive)
     */
    List<LabEquipment> findByNameContainingIgnoreCase(String name);

    /**
     * Find equipment with maintenance due
     */
    @Query("SELECT e FROM LabEquipment e WHERE e.nextMaintenance IS NOT NULL AND e.nextMaintenance <= :date")
    List<LabEquipment> findEquipmentWithMaintenanceDue(@Param("date") LocalDateTime date);

    /**
     * Find equipment with calibration due
     */
    @Query("SELECT e FROM LabEquipment e WHERE e.calibrationDue IS NOT NULL AND e.calibrationDue <= :date")
    List<LabEquipment> findEquipmentWithCalibrationDue(@Param("date") LocalDateTime date);

    /**
     * Find equipment with expired warranty
     */
    @Query("SELECT e FROM LabEquipment e WHERE e.warrantyExpiry IS NOT NULL AND e.warrantyExpiry <= :date")
    List<LabEquipment> findEquipmentWithExpiredWarranty(@Param("date") LocalDateTime date);

    /**
     * Find equipment by status with pagination
     */
    Page<LabEquipment> findByStatus(EquipmentStatus status, Pageable pageable);

    /**
     * Find equipment by type with pagination
     */
    Page<LabEquipment> findByEquipmentType(EquipmentType equipmentType, Pageable pageable);

    /**
     * Search equipment by multiple criteria
     */
    @Query("SELECT e FROM LabEquipment e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:manufacturer IS NULL OR LOWER(e.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
           "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:equipmentType IS NULL OR e.equipmentType = :equipmentType)")
    Page<LabEquipment> searchEquipment(
            @Param("name") String name,
            @Param("manufacturer") String manufacturer,
            @Param("location") String location,
            @Param("status") EquipmentStatus status,
            @Param("equipmentType") EquipmentType equipmentType,
            Pageable pageable);

    /**
     * Count equipment by status
     */
    long countByStatus(EquipmentStatus status);

    /**
     * Count equipment by type
     */
    long countByEquipmentType(EquipmentType equipmentType);

    /**
     * Find equipment requiring attention (maintenance due, calibration due, or warranty expired)
     */
    @Query("SELECT e FROM LabEquipment e WHERE " +
           "(e.nextMaintenance IS NOT NULL AND e.nextMaintenance <= :date) OR " +
           "(e.calibrationDue IS NOT NULL AND e.calibrationDue <= :date) OR " +
           "(e.warrantyExpiry IS NOT NULL AND e.warrantyExpiry <= :date)")
    List<LabEquipment> findEquipmentRequiringAttention(@Param("date") LocalDateTime date);

    /**
     * Get equipment statistics
     */
    @Query("SELECT e.status, COUNT(e) FROM LabEquipment e GROUP BY e.status")
    List<Object[]> getEquipmentStatusStatistics();

    @Query("SELECT e.equipmentType, COUNT(e) FROM LabEquipment e GROUP BY e.equipmentType")
    List<Object[]> getEquipmentTypeStatistics();
}
