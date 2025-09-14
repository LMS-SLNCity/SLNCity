package com.sivalab.laboperations.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for caching to improve application performance
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with predefined cache names
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Define cache names for different components
        cacheManager.setCacheNames(java.util.Arrays.asList(
            // Equipment and Inventory caches
            "equipmentTypes",
            "equipmentStatuses",
            "inventoryCategories",
            "inventoryStatuses",
            "transactionTypes",

            // Statistics caches
            "equipmentStatistics",
            "inventoryStatistics",
            "visitStatistics",
            "billingStatistics",
            "workflowStatistics",

            // Notification caches
            "notificationStatistics",
            "alertStatistics",
            "userNotifications",
            "systemAlerts",

            // System health caches
            "systemHealth",
            "circuitBreakerStatus",
            "rateLimiterStatus",

            // Reference data caches
            "testTemplates",
            "sampleTypes",
            "reportTypes",

            // Audit caches
            "auditStatistics",
            "recentActivities"
        ));
        
        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}
