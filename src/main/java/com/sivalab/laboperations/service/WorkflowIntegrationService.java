package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for integrating workflows between equipment, inventory, and lab operations
 */
@Service
@Transactional
public class WorkflowIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowIntegrationService.class);

    @Autowired
    private LabEquipmentRepository equipmentRepository;

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private LabEquipmentService equipmentService;

    /**
     * Process lab test with equipment and inventory integration
     */
    @CircuitBreaker(name = "workflow", fallbackMethod = "fallbackProcessLabTest")
    @RateLimiter(name = "workflow")
    @Retry(name = "workflow")
    public Map<String, Object> processLabTest(Long testId, String performedBy) {
        logger.info("Processing lab test with ID: {} by {}", testId, performedBy);

        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Lab test not found with ID: " + testId));

        Map<String, Object> result = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> actions = new ArrayList<>();

        // 1. Check required equipment availability
        List<LabEquipment> requiredEquipment = getRequiredEquipmentForTest(labTest);
        Map<String, Object> equipmentStatus = checkEquipmentAvailability(requiredEquipment);
        result.put("equipmentStatus", equipmentStatus);

        if (!(Boolean) equipmentStatus.get("allAvailable")) {
            warnings.add("Some required equipment is not available");
        }

        // 2. Check and consume required inventory
        List<InventoryItem> requiredInventory = getRequiredInventoryForTest(labTest);
        Map<String, Object> inventoryStatus = checkAndConsumeInventory(requiredInventory, performedBy);
        result.put("inventoryStatus", inventoryStatus);

        if (!(Boolean) inventoryStatus.get("allAvailable")) {
            warnings.add("Some required inventory items are not available");
        }

        // 3. Update equipment usage
        updateEquipmentUsage(requiredEquipment, performedBy);
        actions.add("Equipment usage updated");

        // 4. Update test status
        labTest.setStatus(TestStatus.IN_PROGRESS);
        labTestRepository.save(labTest);
        actions.add("Test status updated to IN_PROGRESS");

        result.put("testId", testId);
        result.put("status", "processed");
        result.put("warnings", warnings);
        result.put("actions", actions);
        result.put("processedAt", LocalDateTime.now());
        result.put("processedBy", performedBy);

        return result;
    }

    /**
     * Complete lab test with cleanup
     */
    @CircuitBreaker(name = "workflow", fallbackMethod = "fallbackCompleteLabTest")
    @RateLimiter(name = "workflow")
    @Retry(name = "workflow")
    public Map<String, Object> completeLabTest(Long testId, String completedBy) {
        logger.info("Completing lab test with ID: {} by {}", testId, completedBy);

        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Lab test not found with ID: " + testId));

        Map<String, Object> result = new HashMap<>();
        List<String> actions = new ArrayList<>();

        // 1. Update test status
        labTest.setStatus(TestStatus.COMPLETED);
        labTest.setResultsEnteredAt(LocalDateTime.now());
        labTestRepository.save(labTest);
        actions.add("Test status updated to COMPLETED");

        // 2. Release equipment
        List<LabEquipment> usedEquipment = getRequiredEquipmentForTest(labTest);
        releaseEquipment(usedEquipment);
        actions.add("Equipment released");

        // 3. Check for maintenance requirements
        List<String> maintenanceAlerts = checkMaintenanceRequirements(usedEquipment);
        if (!maintenanceAlerts.isEmpty()) {
            result.put("maintenanceAlerts", maintenanceAlerts);
        }

        // 4. Check inventory levels and generate reorder alerts
        List<String> reorderAlerts = checkReorderRequirements();
        if (!reorderAlerts.isEmpty()) {
            result.put("reorderAlerts", reorderAlerts);
        }

        result.put("testId", testId);
        result.put("status", "completed");
        result.put("actions", actions);
        result.put("completedAt", LocalDateTime.now());
        result.put("completedBy", completedBy);

        return result;
    }

    /**
     * Get comprehensive workflow statistics
     */
    @CircuitBreaker(name = "workflow", fallbackMethod = "fallbackGetWorkflowStatistics")
    @RateLimiter(name = "workflow")
    @Retry(name = "workflow")
    public Map<String, Object> getWorkflowStatistics() {
        logger.debug("Generating workflow statistics");

        Map<String, Object> statistics = new HashMap<>();

        // Equipment utilization
        List<LabEquipment> allEquipment = equipmentRepository.findAll();
        Map<EquipmentStatus, Long> equipmentStatusCounts = allEquipment.stream()
                .collect(Collectors.groupingBy(LabEquipment::getStatus, Collectors.counting()));
        statistics.put("equipmentUtilization", equipmentStatusCounts);

        // Inventory status
        List<InventoryItem> allInventory = inventoryRepository.findAll();
        Map<InventoryStatus, Long> inventoryStatusCounts = allInventory.stream()
                .collect(Collectors.groupingBy(InventoryItem::getStatus, Collectors.counting()));
        statistics.put("inventoryStatus", inventoryStatusCounts);

        // Active tests
        long activeTests = labTestRepository.countByStatus(TestStatus.IN_PROGRESS);
        statistics.put("activeTests", activeTests);

        // Pending tests
        long pendingTests = labTestRepository.countByStatus(TestStatus.PENDING);
        statistics.put("pendingTests", pendingTests);

        // Equipment requiring maintenance
        long maintenanceDue = equipmentRepository.findEquipmentWithMaintenanceDue(LocalDateTime.now()).size();
        statistics.put("equipmentMaintenanceDue", maintenanceDue);

        // Inventory requiring reorder
        long itemsRequiringReorder = inventoryRepository.findItemsRequiringReorder().size();
        statistics.put("inventoryRequiringReorder", itemsRequiringReorder);

        // Low stock items
        long lowStockItems = inventoryRepository.findLowStockItems().size();
        statistics.put("lowStockItems", lowStockItems);

        return statistics;
    }

    // Private helper methods

    private List<LabEquipment> getRequiredEquipmentForTest(LabTest labTest) {
        // This would typically be based on test template requirements
        // For now, return a sample based on test type
        String testName = labTest.getTestTemplate().getName().toLowerCase();
        
        if (testName.contains("blood") || testName.contains("hematology")) {
            return equipmentRepository.findByEquipmentType(EquipmentType.ANALYZER);
        } else if (testName.contains("microscopy") || testName.contains("cell")) {
            return equipmentRepository.findByEquipmentType(EquipmentType.MICROSCOPE);
        } else if (testName.contains("chemistry") || testName.contains("biochemistry")) {
            return equipmentRepository.findByEquipmentType(EquipmentType.SPECTROPHOTOMETER);
        }
        
        return new ArrayList<>();
    }

    private List<InventoryItem> getRequiredInventoryForTest(LabTest labTest) {
        // This would typically be based on test template requirements
        // For now, return common reagents and consumables
        List<InventoryItem> required = new ArrayList<>();
        
        // Add reagents
        required.addAll(inventoryRepository.findByCategory(InventoryCategory.REAGENTS));
        
        // Add tubes/containers
        required.addAll(inventoryRepository.findByCategory(InventoryCategory.TUBES));
        
        return required.stream().limit(3).collect(Collectors.toList()); // Limit for demo
    }

    private Map<String, Object> checkEquipmentAvailability(List<LabEquipment> equipment) {
        Map<String, Object> status = new HashMap<>();
        List<Map<String, Object>> equipmentDetails = new ArrayList<>();
        boolean allAvailable = true;

        for (LabEquipment eq : equipment) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", eq.getId());
            detail.put("name", eq.getName());
            detail.put("status", eq.getStatus());
            detail.put("available", eq.getStatus() == EquipmentStatus.ACTIVE);
            
            if (eq.getStatus() != EquipmentStatus.ACTIVE) {
                allAvailable = false;
            }
            
            equipmentDetails.add(detail);
        }

        status.put("allAvailable", allAvailable);
        status.put("equipment", equipmentDetails);
        return status;
    }

    private Map<String, Object> checkAndConsumeInventory(List<InventoryItem> inventory, String performedBy) {
        Map<String, Object> status = new HashMap<>();
        List<Map<String, Object>> inventoryDetails = new ArrayList<>();
        boolean allAvailable = true;

        for (InventoryItem item : inventory) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", item.getId());
            detail.put("name", item.getName());
            detail.put("currentStock", item.getCurrentStock());
            detail.put("available", item.getCurrentStock() > 0);
            
            if (item.getCurrentStock() > 0) {
                // Consume 1 unit for the test
                try {
                    inventoryService.removeStock(item.getId(), 1, "Lab test consumption", performedBy);
                    detail.put("consumed", 1);
                } catch (Exception e) {
                    logger.warn("Failed to consume inventory item: {}", item.getName(), e);
                    detail.put("consumed", 0);
                    allAvailable = false;
                }
            } else {
                allAvailable = false;
            }
            
            inventoryDetails.add(detail);
        }

        status.put("allAvailable", allAvailable);
        status.put("inventory", inventoryDetails);
        return status;
    }

    private void updateEquipmentUsage(List<LabEquipment> equipment, String usedBy) {
        for (LabEquipment eq : equipment) {
            if (eq.getStatus() == EquipmentStatus.ACTIVE) {
                eq.setStatus(EquipmentStatus.RESERVED);
                equipmentRepository.save(eq);
            }
        }
    }

    private void releaseEquipment(List<LabEquipment> equipment) {
        for (LabEquipment eq : equipment) {
            if (eq.getStatus() == EquipmentStatus.RESERVED) {
                eq.setStatus(EquipmentStatus.ACTIVE);
                equipmentRepository.save(eq);
            }
        }
    }

    private List<String> checkMaintenanceRequirements(List<LabEquipment> equipment) {
        List<String> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (LabEquipment eq : equipment) {
            if (eq.getNextMaintenance() != null && eq.getNextMaintenance().isBefore(now.plusDays(7))) {
                alerts.add("Equipment " + eq.getName() + " requires maintenance within 7 days");
            }
        }
        
        return alerts;
    }

    private List<String> checkReorderRequirements() {
        List<String> alerts = new ArrayList<>();
        List<InventoryItem> reorderItems = inventoryRepository.findItemsRequiringReorder();
        
        for (InventoryItem item : reorderItems) {
            alerts.add("Inventory item " + item.getName() + " requires reordering (Current: " + 
                      item.getCurrentStock() + ", Reorder point: " + item.getReorderPoint() + ")");
        }
        
        return alerts;
    }

    // Fallback methods
    public Map<String, Object> fallbackProcessLabTest(Long testId, String performedBy, Exception ex) {
        logger.error("Fallback: Failed to process lab test", ex);
        return Map.of("error", "Workflow service temporarily unavailable", "testId", testId);
    }

    public Map<String, Object> fallbackCompleteLabTest(Long testId, String completedBy, Exception ex) {
        logger.error("Fallback: Failed to complete lab test", ex);
        return Map.of("error", "Workflow service temporarily unavailable", "testId", testId);
    }

    public Map<String, Object> fallbackGetWorkflowStatistics(Exception ex) {
        logger.error("Fallback: Failed to get workflow statistics", ex);
        return Map.of("error", "Workflow statistics temporarily unavailable");
    }
}
