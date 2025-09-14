package com.sivalab.laboperations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for optional WLAN and network management features
 * This makes network features plug-and-play rather than mandatory
 */
@Configuration
@ConfigurationProperties(prefix = "lab.network")
public class NetworkConfig {

    private boolean enabled = false;  // Disabled by default - plug and play
    private Monitoring monitoring = new Monitoring();
    private MachineId machineId = new MachineId();
    private Features features = new Features();
    private Fallback fallback = new Fallback();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Monitoring getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Monitoring monitoring) {
        this.monitoring = monitoring;
    }

    public MachineId getMachineId() {
        return machineId;
    }

    public void setMachineId(MachineId machineId) {
        this.machineId = machineId;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Fallback getFallback() {
        return fallback;
    }

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
    }

    /**
     * Check if network monitoring is enabled and available
     */
    public boolean isNetworkMonitoringAvailable() {
        return enabled && monitoring != null;
    }

    /**
     * Check if machine ID features are enabled and available
     */
    public boolean isMachineIdManagementAvailable() {
        return enabled && machineId != null;
    }

    /**
     * Check if the system should gracefully degrade when network features fail
     */
    public boolean shouldGracefullyDegrade() {
        return fallback != null && fallback.gracefulDegradation;
    }

    /**
     * Check if network errors should be skipped during startup
     */
    public boolean shouldSkipNetworkErrors() {
        return fallback != null && fallback.skipNetworkErrors;
    }

    // Inner classes for configuration sections
    public static class Monitoring {
        private int interval = 60;  // seconds
        private boolean autoDetection = true;
        private int retryAttempts = 3;
        private int timeout = 30;  // seconds

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public boolean isAutoDetection() {
            return autoDetection;
        }

        public void setAutoDetection(boolean autoDetection) {
            this.autoDetection = autoDetection;
        }

        public int getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class MachineId {
        private boolean validation = false;  // Optional - disabled by default
        private boolean autoRegister = false;  // Optional - disabled by default
        private boolean duplicateCheck = false;  // Optional - disabled by default

        public boolean isValidation() {
            return validation;
        }

        public void setValidation(boolean validation) {
            this.validation = validation;
        }

        public boolean isAutoRegister() {
            return autoRegister;
        }

        public void setAutoRegister(boolean autoRegister) {
            this.autoRegister = autoRegister;
        }

        public boolean isDuplicateCheck() {
            return duplicateCheck;
        }

        public void setDuplicateCheck(boolean duplicateCheck) {
            this.duplicateCheck = duplicateCheck;
        }
    }

    public static class Features {
        private boolean connectivityTest = false;  // Optional - disabled by default
        private boolean issueAutoDetection = false;  // Optional - disabled by default
        private boolean statisticsCollection = true;  // Statistics always available

        public boolean isConnectivityTest() {
            return connectivityTest;
        }

        public void setConnectivityTest(boolean connectivityTest) {
            this.connectivityTest = connectivityTest;
        }

        public boolean isIssueAutoDetection() {
            return issueAutoDetection;
        }

        public void setIssueAutoDetection(boolean issueAutoDetection) {
            this.issueAutoDetection = issueAutoDetection;
        }

        public boolean isStatisticsCollection() {
            return statisticsCollection;
        }

        public void setStatisticsCollection(boolean statisticsCollection) {
            this.statisticsCollection = statisticsCollection;
        }
    }

    public static class Fallback {
        private boolean gracefulDegradation = true;  // System continues without network features
        private boolean skipNetworkErrors = true;    // Don't fail startup on network issues

        public boolean isGracefulDegradation() {
            return gracefulDegradation;
        }

        public void setGracefulDegradation(boolean gracefulDegradation) {
            this.gracefulDegradation = gracefulDegradation;
        }

        public boolean isSkipNetworkErrors() {
            return skipNetworkErrors;
        }

        public void setSkipNetworkErrors(boolean skipNetworkErrors) {
            this.skipNetworkErrors = skipNetworkErrors;
        }
    }
}
