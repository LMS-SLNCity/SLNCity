package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.service.MachineIdIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing machine ID issues
 * This controller is optional and only enabled when network monitoring is configured
 */
@RestController
@RequestMapping("/api/v1/machine-id-issues")
@Tag(name = "Machine ID Issues", description = "Machine identification issue management operations")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "lab.network.enabled", havingValue = "true", matchIfMissing = false)
public class MachineIdIssueController {

    private static final Logger logger = LoggerFactory.getLogger(MachineIdIssueController.class);

    @Autowired
    private MachineIdIssueService machineIdIssueService;

    @Operation(summary = "Create new machine ID issue", description = "Report a new machine identification issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Machine ID issue created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<MachineIdIssue> createIssue(@Valid @RequestBody MachineIdIssue issue) {
        try {
            logger.info("Creating machine ID issue: {}", issue.getTitle());
            MachineIdIssue createdIssue = machineIdIssueService.createIssue(issue);
            return new ResponseEntity<>(createdIssue, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating machine ID issue", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get machine ID issue by ID", description = "Retrieve machine ID issue details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Machine ID issue found"),
            @ApiResponse(responseCode = "404", description = "Machine ID issue not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MachineIdIssue> getIssueById(
            @Parameter(description = "Machine ID issue ID") @PathVariable Long id) {
        try {
            Optional<MachineIdIssue> issue = machineIdIssueService.getIssueById(id);
            return issue.map(iss -> ResponseEntity.ok(iss))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving machine ID issue by ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get all machine ID issues", description = "Retrieve all machine ID issues")
    @ApiResponse(responseCode = "200", description = "Machine ID issues retrieved successfully")
    @GetMapping
    public ResponseEntity<List<MachineIdIssue>> getAllIssues() {
        try {
            List<MachineIdIssue> issues = machineIdIssueService.getAllIssues();
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error retrieving all machine ID issues", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get open machine ID issues", description = "Retrieve all open machine ID issues")
    @ApiResponse(responseCode = "200", description = "Open machine ID issues retrieved successfully")
    @GetMapping("/open")
    public ResponseEntity<List<MachineIdIssue>> getOpenIssues() {
        try {
            List<MachineIdIssue> issues = machineIdIssueService.getOpenIssues();
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error retrieving open machine ID issues", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issues by equipment", description = "Retrieve machine ID issues for specific equipment")
    @ApiResponse(responseCode = "200", description = "Machine ID issues retrieved successfully")
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<MachineIdIssue>> getIssuesByEquipment(
            @Parameter(description = "Equipment ID") @PathVariable Long equipmentId) {
        try {
            List<MachineIdIssue> issues = machineIdIssueService.getIssuesByEquipment(equipmentId);
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error retrieving issues for equipment ID: {}", equipmentId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issues by severity", description = "Retrieve machine ID issues by severity level")
    @ApiResponse(responseCode = "200", description = "Machine ID issues retrieved successfully")
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<MachineIdIssue>> getIssuesBySeverity(
            @Parameter(description = "Issue severity") @PathVariable IssueSeverity severity) {
        try {
            List<MachineIdIssue> issues = machineIdIssueService.getIssuesBySeverity(severity);
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error retrieving issues by severity: {}", severity, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get high priority issues", description = "Retrieve high priority machine ID issues")
    @ApiResponse(responseCode = "200", description = "High priority machine ID issues retrieved successfully")
    @GetMapping("/high-priority")
    public ResponseEntity<List<MachineIdIssue>> getHighPriorityIssues() {
        try {
            List<MachineIdIssue> issues = machineIdIssueService.getHighPriorityIssues();
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error retrieving high priority issues", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update issue status", description = "Update the status of a machine ID issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Machine ID issue not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<MachineIdIssue> updateIssueStatus(
            @Parameter(description = "Machine ID issue ID") @PathVariable Long id,
            @Parameter(description = "New issue status") @RequestParam IssueStatus status,
            @Parameter(description = "Person resolving the issue") @RequestParam(required = false) String resolvedBy) {
        try {
            MachineIdIssue updatedIssue = machineIdIssueService.updateIssueStatus(id, status, resolvedBy);
            return ResponseEntity.ok(updatedIssue);
        } catch (IllegalArgumentException e) {
            logger.error("Machine ID issue not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error updating issue status for ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Assign issue", description = "Assign a machine ID issue to a technician")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Machine ID issue not found")
    })
    @PutMapping("/{id}/assign")
    public ResponseEntity<MachineIdIssue> assignIssue(
            @Parameter(description = "Machine ID issue ID") @PathVariable Long id,
            @Parameter(description = "Person to assign the issue to") @RequestParam String assignedTo) {
        try {
            MachineIdIssue updatedIssue = machineIdIssueService.assignIssue(id, assignedTo);
            return ResponseEntity.ok(updatedIssue);
        } catch (IllegalArgumentException e) {
            logger.error("Machine ID issue not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error assigning issue ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Escalate issue", description = "Escalate a machine ID issue to higher level support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue escalated successfully"),
            @ApiResponse(responseCode = "404", description = "Machine ID issue not found")
    })
    @PutMapping("/{id}/escalate")
    public ResponseEntity<MachineIdIssue> escalateIssue(
            @Parameter(description = "Machine ID issue ID") @PathVariable Long id,
            @Parameter(description = "Person/team to escalate to") @RequestParam String escalatedTo) {
        try {
            MachineIdIssue updatedIssue = machineIdIssueService.escalateIssue(id, escalatedTo);
            return ResponseEntity.ok(updatedIssue);
        } catch (IllegalArgumentException e) {
            logger.error("Machine ID issue not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error escalating issue ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Record issue occurrence", description = "Record another occurrence of an existing issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue occurrence recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Machine ID issue not found")
    })
    @PostMapping("/{id}/occurrence")
    public ResponseEntity<MachineIdIssue> recordIssueOccurrence(
            @Parameter(description = "Machine ID issue ID") @PathVariable Long id) {
        try {
            MachineIdIssue updatedIssue = machineIdIssueService.recordIssueOccurrence(id);
            return ResponseEntity.ok(updatedIssue);
        } catch (IllegalArgumentException e) {
            logger.error("Machine ID issue not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error recording occurrence for issue ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Auto-detect issues", description = "Automatically detect machine ID and network issues")
    @ApiResponse(responseCode = "200", description = "Issues auto-detected successfully")
    @PostMapping("/auto-detect")
    public ResponseEntity<List<MachineIdIssue>> autoDetectIssues() {
        try {
            List<MachineIdIssue> detectedIssues = machineIdIssueService.autoDetectIssues();
            return ResponseEntity.ok(detectedIssues);
        } catch (Exception e) {
            logger.error("Error auto-detecting issues", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issue statistics", description = "Retrieve comprehensive machine ID issue statistics")
    @ApiResponse(responseCode = "200", description = "Issue statistics retrieved successfully")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getIssueStatistics() {
        try {
            Map<String, Object> statistics = machineIdIssueService.getIssueStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error retrieving issue statistics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issue types", description = "Get all available issue types")
    @ApiResponse(responseCode = "200", description = "Issue types retrieved successfully")
    @GetMapping("/types")
    public ResponseEntity<IssueType[]> getIssueTypes() {
        return ResponseEntity.ok(IssueType.values());
    }

    @Operation(summary = "Get issue severities", description = "Get all available issue severities")
    @ApiResponse(responseCode = "200", description = "Issue severities retrieved successfully")
    @GetMapping("/severities")
    public ResponseEntity<IssueSeverity[]> getIssueSeverities() {
        return ResponseEntity.ok(IssueSeverity.values());
    }

    @Operation(summary = "Get issue statuses", description = "Get all available issue statuses")
    @ApiResponse(responseCode = "200", description = "Issue statuses retrieved successfully")
    @GetMapping("/statuses")
    public ResponseEntity<IssueStatus[]> getIssueStatuses() {
        return ResponseEntity.ok(IssueStatus.values());
    }
}
