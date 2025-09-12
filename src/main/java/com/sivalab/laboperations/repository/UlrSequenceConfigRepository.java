package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.UlrSequenceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UlrSequenceConfigRepository extends JpaRepository<UlrSequenceConfig, Long> {
    
    /**
     * Find active configuration for a specific year
     */
    Optional<UlrSequenceConfig> findByYearAndIsActive(Integer year, Boolean isActive);

    /**
     * Find configuration for current year
     */
    @Query("SELECT c FROM UlrSequenceConfig c WHERE c.year = EXTRACT(YEAR FROM CURRENT_DATE) AND c.isActive = true")
    Optional<UlrSequenceConfig> findCurrentYearConfig();

    /**
     * Get next sequence number for current year
     */
    @Modifying
    @Query("UPDATE UlrSequenceConfig c SET c.sequenceNumber = c.sequenceNumber + 1, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.year = :year AND c.isActive = true")
    int incrementSequenceNumber(@Param("year") Integer year);

    /**
     * Get current sequence number for year
     */
    @Query("SELECT c.sequenceNumber FROM UlrSequenceConfig c WHERE c.year = :year AND c.isActive = true")
    Optional<Integer> getCurrentSequenceNumber(@Param("year") Integer year);

    /**
     * Check if configuration exists for year
     */
    boolean existsByYearAndIsActive(Integer year, Boolean isActive);

    /**
     * Deactivate all configurations for a year
     */
    @Modifying
    @Query("UPDATE UlrSequenceConfig c SET c.isActive = false WHERE c.year = :year")
    int deactivateConfigsForYear(@Param("year") Integer year);
}
