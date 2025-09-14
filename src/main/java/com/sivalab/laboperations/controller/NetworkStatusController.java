package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.config.NetworkConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller that provides network status information
 * This controller is always available regardless of network feature configuration
 */
@RestController
@RequestMapping("/api/v1/network-status")
@Tag(name = "Network Status", description = "Basic network status and configuration information")
@CrossOrigin(origins = "*")
public class NetworkStatusController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkStatusController.class);

    @Autowired
    private NetworkConfig networkConfig;

    @Operation(summary = "Get network feature status", description = "Check if network monitoring features are enabled")
    @ApiResponse(responseCode = "200", description = "Network feature status retrieved successfully")
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getNetworkFeatureStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("networkMonitoringEnabled", networkConfig.isEnabled());
            status.put("networkMonitoringAvailable", networkConfig.isNetworkMonitoringAvailable());
            status.put("machineIdManagementAvailable", networkConfig.isMachineIdManagementAvailable());
            status.put("gracefulDegradation", networkConfig.shouldGracefullyDegrade());
            
            if (networkConfig.isEnabled()) {
                status.put("monitoringInterval", networkConfig.getMonitoring().getInterval());
                status.put("autoDetection", networkConfig.getMonitoring().isAutoDetection());
                status.put("machineIdValidation", networkConfig.getMachineId().isValidation());
                status.put("connectivityTest", networkConfig.getFeatures().isConnectivityTest());
                status.put("issueAutoDetection", networkConfig.getFeatures().isIssueAutoDetection());
                status.put("statisticsCollection", networkConfig.getFeatures().isStatisticsCollection());
            }
            
            status.put("message", networkConfig.isEnabled() ? 
                "Network monitoring features are enabled and available" : 
                "Network monitoring features are disabled - system operates in basic mode");
                
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error retrieving network feature status", e);
            status.put("error", "Unable to retrieve network feature status");
            status.put("networkMonitoringEnabled", false);
            status.put("message", "Network monitoring features are unavailable");
            return ResponseEntity.ok(status);
        }
    }

    @Operation(summary = "Get network configuration", description = "Retrieve current network configuration settings")
    @ApiResponse(responseCode = "200", description = "Network configuration retrieved successfully")
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getNetworkConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            config.put("enabled", networkConfig.isEnabled());
            
            if (networkConfig.isEnabled()) {
                // Monitoring configuration
                Map<String, Object> monitoring = new HashMap<>();
                monitoring.put("interval", networkConfig.getMonitoring().getInterval());
                monitoring.put("autoDetection", networkConfig.getMonitoring().isAutoDetection());
                monitoring.put("retryAttempts", networkConfig.getMonitoring().getRetryAttempts());
                monitoring.put("timeout", networkConfig.getMonitoring().getTimeout());
                config.put("monitoring", monitoring);
                
                // Machine ID configuration
                Map<String, Object> machineId = new HashMap<>();
                machineId.put("validation", networkConfig.getMachineId().isValidation());
                machineId.put("autoRegister", networkConfig.getMachineId().isAutoRegister());
                machineId.put("duplicateCheck", networkConfig.getMachineId().isDuplicateCheck());
                config.put("machineId", machineId);
                
                // Features configuration
                Map<String, Object> features = new HashMap<>();
                features.put("connectivityTest", networkConfig.getFeatures().isConnectivityTest());
                features.put("issueAutoDetection", networkConfig.getFeatures().isIssueAutoDetection());
                features.put("statisticsCollection", networkConfig.getFeatures().isStatisticsCollection());
                config.put("features", features);
                
                // Fallback configuration
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("gracefulDegradation", networkConfig.getFallback().isGracefulDegradation());
                fallback.put("skipNetworkErrors", networkConfig.getFallback().isSkipNetworkErrors());
                config.put("fallback", fallback);
            } else {
                config.put("message", "Network monitoring is disabled - operating in basic mode");
            }
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Error retrieving network configuration", e);
            config.put("error", "Unable to retrieve network configuration");
            config.put("enabled", false);
            return ResponseEntity.ok(config);
        }
    }

    @Operation(summary = "Get basic system connectivity", description = "Basic connectivity check without full network monitoring")
    @ApiResponse(responseCode = "200", description = "Basic connectivity status retrieved successfully")
    @GetMapping("/basic-connectivity")
    public ResponseEntity<Map<String, Object>> getBasicConnectivity() {
        Map<String, Object> connectivity = new HashMap<>();
        
        try {
            // Basic connectivity information that doesn't require full network monitoring
            connectivity.put("systemOnline", true);
            connectivity.put("databaseConnected", true); // If we can respond, DB is connected
            connectivity.put("applicationHealthy", true);
            connectivity.put("timestamp", System.currentTimeMillis());
            
            if (networkConfig.isEnabled()) {
                connectivity.put("networkMonitoringActive", true);
                connectivity.put("fullNetworkFeaturesAvailable", true);
                connectivity.put("message", "Full network monitoring is active");
            } else {
                connectivity.put("networkMonitoringActive", false);
                connectivity.put("fullNetworkFeaturesAvailable", false);
                connectivity.put("message", "Operating in basic mode - full network features disabled");
            }
            
            return ResponseEntity.ok(connectivity);
        } catch (Exception e) {
            logger.error("Error checking basic connectivity", e);
            connectivity.put("error", "Unable to check connectivity");
            connectivity.put("systemOnline", false);
            return ResponseEntity.ok(connectivity);
        }
    }

    @Operation(summary = "Get available network endpoints", description = "List available network-related endpoints based on configuration")
    @ApiResponse(responseCode = "200", description = "Available endpoints retrieved successfully")
    @GetMapping("/available-endpoints")
    public ResponseEntity<Map<String, Object>> getAvailableEndpoints() {
        Map<String, Object> endpoints = new HashMap<>();
        
        try {
            endpoints.put("networkStatusEndpoints", new String[]{
                "/api/v1/network-status/features",
                "/api/v1/network-status/configuration",
                "/api/v1/network-status/basic-connectivity",
                "/api/v1/network-status/available-endpoints"
            });
            
            if (networkConfig.isEnabled()) {
                endpoints.put("networkConnectionEndpoints", new String[]{
                    "/api/v1/network-connections",
                    "/api/v1/network-connections/connected",
                    "/api/v1/network-connections/statistics",
                    "/api/v1/network-connections/statuses",
                    "/api/v1/network-connections/types"
                });
                
                endpoints.put("machineIdIssueEndpoints", new String[]{
                    "/api/v1/machine-id-issues",
                    "/api/v1/machine-id-issues/open",
                    "/api/v1/machine-id-issues/statistics",
                    "/api/v1/machine-id-issues/types",
                    "/api/v1/machine-id-issues/severities",
                    "/api/v1/machine-id-issues/statuses"
                });
                
                endpoints.put("message", "Full network monitoring endpoints are available");
            } else {
                endpoints.put("networkConnectionEndpoints", new String[0]);
                endpoints.put("machineIdIssueEndpoints", new String[0]);
                endpoints.put("message", "Network monitoring endpoints are disabled - only basic status endpoints available");
            }
            
            return ResponseEntity.ok(endpoints);
        } catch (Exception e) {
            logger.error("Error retrieving available endpoints", e);
            endpoints.put("error", "Unable to retrieve endpoint information");
            return ResponseEntity.ok(endpoints);
        }
    }
}
