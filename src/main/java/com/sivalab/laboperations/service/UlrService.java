package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.UlrSequenceConfig;
import com.sivalab.laboperations.repository.UlrSequenceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for generating NABL-compliant Unique Laboratory Report (ULR) numbers
 */
@Service
@Transactional
public class UlrService {
    
    private final UlrSequenceConfigRepository configRepository;
    
    @Autowired
    public UlrService(UlrSequenceConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
    
    /**
     * Generate next ULR number for current year
     * Format: PREFIX/YEAR/SEQUENCE (e.g., SLN/2025/000001)
     */
    public String generateUlrNumber() {
        int currentYear = LocalDate.now().getYear();
        return generateUlrNumber(currentYear);
    }
    
    /**
     * Generate ULR number for specific year
     */
    public String generateUlrNumber(int year) {
        UlrSequenceConfig config = getOrCreateConfig(year);
        
        // Increment sequence number
        int nextSequence = config.getSequenceNumber();
        config.setSequenceNumber(nextSequence + 1);
        configRepository.save(config);
        
        // Generate ULR number
        return formatUlrNumber(config.getPrefix(), year, nextSequence);
    }
    
    /**
     * Get or create configuration for year
     */
    private UlrSequenceConfig getOrCreateConfig(int year) {
        Optional<UlrSequenceConfig> existingConfig = configRepository.findByYearAndIsActive(year, true);
        
        if (existingConfig.isPresent()) {
            return existingConfig.get();
        }
        
        // Create new configuration for the year
        UlrSequenceConfig newConfig = new UlrSequenceConfig(year, "SLN");
        return configRepository.save(newConfig);
    }
    
    /**
     * Format ULR number according to NABL standards
     * Format: PREFIX/YEAR/SEQUENCE (e.g., SLN/2025/000001)
     */
    private String formatUlrNumber(String prefix, int year, int sequence) {
        return String.format("%s/%d/%06d", prefix, year, sequence);
    }
    
    /**
     * Validate ULR number format
     */
    public boolean isValidUlrFormat(String ulrNumber) {
        if (ulrNumber == null || ulrNumber.trim().isEmpty()) {
            return false;
        }
        
        // Expected format: PREFIX/YEAR/SEQUENCE
        String[] parts = ulrNumber.split("/");
        if (parts.length != 3) {
            return false;
        }
        
        try {
            // Validate year (should be 4 digits)
            int year = Integer.parseInt(parts[1]);
            if (year < 2000 || year > 2100) {
                return false;
            }
            
            // Validate sequence (should be numeric)
            int sequence = Integer.parseInt(parts[2]);
            if (sequence < 1) {
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Extract year from ULR number
     */
    public Optional<Integer> extractYearFromUlr(String ulrNumber) {
        if (!isValidUlrFormat(ulrNumber)) {
            return Optional.empty();
        }
        
        try {
            String[] parts = ulrNumber.split("/");
            return Optional.of(Integer.parseInt(parts[1]));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Extract sequence number from ULR number
     */
    public Optional<Integer> extractSequenceFromUlr(String ulrNumber) {
        if (!isValidUlrFormat(ulrNumber)) {
            return Optional.empty();
        }
        
        try {
            String[] parts = ulrNumber.split("/");
            return Optional.of(Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get current sequence number for year
     */
    @Transactional(readOnly = true)
    public int getCurrentSequenceNumber(int year) {
        return configRepository.getCurrentSequenceNumber(year).orElse(0);
    }
    
    /**
     * Get current year configuration
     */
    @Transactional(readOnly = true)
    public Optional<UlrSequenceConfig> getCurrentYearConfig() {
        return configRepository.findCurrentYearConfig();
    }
    
    /**
     * Update ULR prefix for current year
     */
    public void updatePrefix(String newPrefix) {
        int currentYear = LocalDate.now().getYear();
        UlrSequenceConfig config = getOrCreateConfig(currentYear);
        config.setPrefix(newPrefix);
        configRepository.save(config);
    }
    
    /**
     * Reset sequence for new year (typically called at year end)
     */
    public void resetSequenceForNewYear(int year) {
        // Deactivate old configurations
        configRepository.deactivateConfigsForYear(year - 1);
        
        // Create new configuration for the year
        UlrSequenceConfig newConfig = new UlrSequenceConfig(year, "SLN");
        configRepository.save(newConfig);
    }
}
