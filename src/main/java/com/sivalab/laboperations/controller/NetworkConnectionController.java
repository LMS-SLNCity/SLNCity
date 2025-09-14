package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.*;
import com.sivalab.laboperations.service.NetworkConnectionService;
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
 * REST Controller for managing network connections
 * This controller is optional and only enabled when network monitoring is configured
 */
@RestController
@RequestMapping("/api/v1/network-connections")
@Tag(name = "Network Connections", description = "WLAN connectivity and network management operations")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "lab.network.enabled", havingValue = "true", matchIfMissing = false)
public class NetworkConnectionController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkConnectionController.class);

    @Autowired
    private NetworkConnectionService networkConnectionService;

    @Operation(summary = "Create new network connection", description = "Register a new network connection for lab equipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Network connection created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Machine ID or MAC address already exists")
    })
    @PostMapping
    public ResponseEntity<NetworkConnection> createConnection(
            @Valid @RequestBody NetworkConnection connection) {
        try {
            logger.info("Creating network connection for machine ID: {}", connection.getMachineId());
            NetworkConnection createdConnection = networkConnectionService.createConnection(connection);
            return new ResponseEntity<>(createdConnection, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for network connection creation: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating network connection", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get network connection by ID", description = "Retrieve network connection details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Network connection found"),
            @ApiResponse(responseCode = "404", description = "Network connection not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NetworkConnection> getConnectionById(
            @Parameter(description = "Network connection ID") @PathVariable Long id) {
        try {
            Optional<NetworkConnection> connection = networkConnectionService.getConnectionById(id);
            return connection.map(conn -> ResponseEntity.ok(conn))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving network connection by ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get network connection by machine ID", description = "Retrieve network connection by machine ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Network connection found"),
            @ApiResponse(responseCode = "404", description = "Network connection not found")
    })
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<NetworkConnection> getConnectionByMachineId(
            @Parameter(description = "Machine ID") @PathVariable String machineId) {
        try {
            Optional<NetworkConnection> connection = networkConnectionService.getConnectionByMachineId(machineId);
            return connection.map(conn -> ResponseEntity.ok(conn))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error retrieving network connection by machine ID: {}", machineId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get all network connections", description = "Retrieve all network connections")
    @ApiResponse(responseCode = "200", description = "Network connections retrieved successfully")
    @GetMapping
    public ResponseEntity<List<NetworkConnection>> getAllConnections() {
        try {
            List<NetworkConnection> connections = networkConnectionService.getAllConnections();
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            logger.error("Error retrieving all network connections", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get connections by equipment", description = "Retrieve network connections for specific equipment")
    @ApiResponse(responseCode = "200", description = "Network connections retrieved successfully")
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<NetworkConnection>> getConnectionsByEquipment(
            @Parameter(description = "Equipment ID") @PathVariable Long equipmentId) {
        try {
            List<NetworkConnection> connections = networkConnectionService.getConnectionsByEquipment(equipmentId);
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            logger.error("Error retrieving connections for equipment ID: {}", equipmentId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get connections by status", description = "Retrieve network connections by connection status")
    @ApiResponse(responseCode = "200", description = "Network connections retrieved successfully")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<NetworkConnection>> getConnectionsByStatus(
            @Parameter(description = "Connection status") @PathVariable ConnectionStatus status) {
        try {
            List<NetworkConnection> connections = networkConnectionService.getConnectionsByStatus(status);
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            logger.error("Error retrieving connections by status: {}", status, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get connected connections", description = "Retrieve all currently connected network connections")
    @ApiResponse(responseCode = "200", description = "Connected network connections retrieved successfully")
    @GetMapping("/connected")
    public ResponseEntity<List<NetworkConnection>> getConnectedConnections() {
        try {
            List<NetworkConnection> connections = networkConnectionService.getConnectedConnections();
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            logger.error("Error retrieving connected connections", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get connections requiring attention", description = "Retrieve network connections that need attention")
    @ApiResponse(responseCode = "200", description = "Problematic network connections retrieved successfully")
    @GetMapping("/attention-required")
    public ResponseEntity<List<NetworkConnection>> getConnectionsRequiringAttention() {
        try {
            List<NetworkConnection> connections = networkConnectionService.getConnectionsRequiringAttention();
            return ResponseEntity.ok(connections);
        } catch (Exception e) {
            logger.error("Error retrieving connections requiring attention", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update connection status", description = "Update the status of a network connection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Network connection not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<NetworkConnection> updateConnectionStatus(
            @Parameter(description = "Network connection ID") @PathVariable Long id,
            @Parameter(description = "New connection status") @RequestParam ConnectionStatus status) {
        try {
            NetworkConnection updatedConnection = networkConnectionService.updateConnectionStatus(id, status);
            return ResponseEntity.ok(updatedConnection);
        } catch (IllegalArgumentException e) {
            logger.error("Network connection not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error updating connection status for ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Update connection diagnostics", description = "Update diagnostic information for a network connection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection diagnostics updated successfully"),
            @ApiResponse(responseCode = "404", description = "Network connection not found")
    })
    @PutMapping("/{id}/diagnostics")
    public ResponseEntity<NetworkConnection> updateConnectionDiagnostics(
            @Parameter(description = "Network connection ID") @PathVariable Long id,
            @Parameter(description = "Signal strength in dBm") @RequestParam(required = false) Integer signalStrength,
            @Parameter(description = "Ping response time in ms") @RequestParam(required = false) Integer pingResponse,
            @Parameter(description = "Packet loss percentage") @RequestParam(required = false) Double packetLoss) {
        try {
            NetworkConnection updatedConnection = networkConnectionService.updateConnectionDiagnostics(
                    id, signalStrength, pingResponse, packetLoss);
            return ResponseEntity.ok(updatedConnection);
        } catch (IllegalArgumentException e) {
            logger.error("Network connection not found: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error updating connection diagnostics for ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Perform connectivity test", description = "Perform network connectivity test for a machine")
    @ApiResponse(responseCode = "200", description = "Connectivity test completed successfully")
    @PostMapping("/test/{machineId}")
    public ResponseEntity<Map<String, Object>> performConnectivityTest(
            @Parameter(description = "Machine ID") @PathVariable String machineId) {
        try {
            Map<String, Object> testResults = networkConnectionService.performConnectivityTest(machineId);
            return ResponseEntity.ok(testResults);
        } catch (IllegalArgumentException e) {
            logger.error("Machine not found for connectivity test: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error performing connectivity test for machine ID: {}", machineId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get network statistics", description = "Retrieve comprehensive network connection statistics")
    @ApiResponse(responseCode = "200", description = "Network statistics retrieved successfully")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNetworkStatistics() {
        try {
            Map<String, Object> statistics = networkConnectionService.getNetworkStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error retrieving network statistics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Detect network issues", description = "Automatically detect network connectivity issues")
    @ApiResponse(responseCode = "200", description = "Network issues detected successfully")
    @GetMapping("/issues/detect")
    public ResponseEntity<List<Map<String, Object>>> detectNetworkIssues() {
        try {
            List<Map<String, Object>> issues = networkConnectionService.detectNetworkIssues();
            return ResponseEntity.ok(issues);
        } catch (Exception e) {
            logger.error("Error detecting network issues", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get connection statuses", description = "Get all available connection statuses")
    @ApiResponse(responseCode = "200", description = "Connection statuses retrieved successfully")
    @GetMapping("/statuses")
    public ResponseEntity<ConnectionStatus[]> getConnectionStatuses() {
        return ResponseEntity.ok(ConnectionStatus.values());
    }

    @Operation(summary = "Get connection types", description = "Get all available connection types")
    @ApiResponse(responseCode = "200", description = "Connection types retrieved successfully")
    @GetMapping("/types")
    public ResponseEntity<ConnectionType[]> getConnectionTypes() {
        return ResponseEntity.ok(ConnectionType.values());
    }
}
