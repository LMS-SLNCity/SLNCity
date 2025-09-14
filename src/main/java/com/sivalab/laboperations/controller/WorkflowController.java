package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.service.WorkflowIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for workflow integration operations
 */
@RestController
@RequestMapping("/api/v1/workflow")
@Tag(name = "Workflow Integration", description = "Integrated workflow operations for lab processes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private WorkflowIntegrationService workflowService;

    /**
     * Process lab test with equipment and inventory integration
     */
    @PostMapping("/tests/{testId}/process")
    @Operation(summary = "Process lab test", description = "Process lab test with equipment and inventory integration")
    public ResponseEntity<Map<String, Object>> processLabTest(
            @Parameter(description = "Test ID") @PathVariable Long testId,
            @Parameter(description = "Performed by") @RequestParam String performedBy) {
        try {
            if (performedBy == null || performedBy.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "performedBy parameter is required"));
            }

            Map<String, Object> result = workflowService.processLabTest(testId, performedBy);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for processing lab test", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing lab test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process lab test", "message", e.getMessage()));
        }
    }

    /**
     * Complete lab test with cleanup
     */
    @PostMapping("/tests/{testId}/complete")
    @Operation(summary = "Complete lab test", description = "Complete lab test with equipment and inventory cleanup")
    public ResponseEntity<Map<String, Object>> completeLabTest(
            @Parameter(description = "Test ID") @PathVariable Long testId,
            @Parameter(description = "Completed by") @RequestParam String completedBy) {
        try {
            if (completedBy == null || completedBy.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "completedBy parameter is required"));
            }

            Map<String, Object> result = workflowService.completeLabTest(testId, completedBy);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for completing lab test", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error completing lab test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete lab test", "message", e.getMessage()));
        }
    }

    /**
     * Get workflow statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get workflow statistics", description = "Retrieve comprehensive workflow statistics")
    public ResponseEntity<Map<String, Object>> getWorkflowStatistics() {
        try {
            Map<String, Object> statistics = workflowService.getWorkflowStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting workflow statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve workflow statistics", "message", e.getMessage()));
        }
    }

    /**
     * Get workflow health check
     */
    @GetMapping("/health")
    @Operation(summary = "Get workflow health", description = "Check workflow system health")
    public ResponseEntity<Map<String, Object>> getWorkflowHealth() {
        try {
            Map<String, Object> health = Map.of(
                    "status", "UP",
                    "service", "WorkflowIntegrationService",
                    "timestamp", System.currentTimeMillis(),
                    "message", "Workflow integration service is operational"
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Error checking workflow health", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "DOWN",
                            "service", "WorkflowIntegrationService",
                            "timestamp", System.currentTimeMillis(),
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * Get equipment utilization summary
     */
    @GetMapping("/equipment/utilization")
    @Operation(summary = "Get equipment utilization", description = "Retrieve equipment utilization summary")
    public ResponseEntity<Map<String, Object>> getEquipmentUtilization() {
        try {
            Map<String, Object> statistics = workflowService.getWorkflowStatistics();
            Map<String, Object> utilization = Map.of(
                    "equipmentUtilization", statistics.get("equipmentUtilization"),
                    "maintenanceDue", statistics.get("equipmentMaintenanceDue"),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(utilization);
        } catch (Exception e) {
            logger.error("Error getting equipment utilization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve equipment utilization", "message", e.getMessage()));
        }
    }

    /**
     * Get inventory consumption summary
     */
    @GetMapping("/inventory/consumption")
    @Operation(summary = "Get inventory consumption", description = "Retrieve inventory consumption summary")
    public ResponseEntity<Map<String, Object>> getInventoryConsumption() {
        try {
            Map<String, Object> statistics = workflowService.getWorkflowStatistics();
            Map<String, Object> consumption = Map.of(
                    "inventoryStatus", statistics.get("inventoryStatus"),
                    "reorderRequired", statistics.get("inventoryRequiringReorder"),
                    "lowStock", statistics.get("lowStockItems"),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(consumption);
        } catch (Exception e) {
            logger.error("Error getting inventory consumption", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve inventory consumption", "message", e.getMessage()));
        }
    }

    /**
     * Get active operations summary
     */
    @GetMapping("/operations/active")
    @Operation(summary = "Get active operations", description = "Retrieve active lab operations summary")
    public ResponseEntity<Map<String, Object>> getActiveOperations() {
        try {
            Map<String, Object> statistics = workflowService.getWorkflowStatistics();
            Map<String, Object> operations = Map.of(
                    "activeTests", statistics.get("activeTests"),
                    "pendingTests", statistics.get("pendingTests"),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(operations);
        } catch (Exception e) {
            logger.error("Error getting active operations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve active operations", "message", e.getMessage()));
        }
    }
}
