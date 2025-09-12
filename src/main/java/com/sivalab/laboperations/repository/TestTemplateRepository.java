package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.TestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestTemplateRepository extends JpaRepository<TestTemplate, Long> {
    
    /**
     * Find test template by name (case-insensitive)
     */
    Optional<TestTemplate> findByNameIgnoreCase(String name);
    
    /**
     * Find test templates by name containing (case-insensitive)
     */
    List<TestTemplate> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find test templates by price range
     */
    List<TestTemplate> findByBasePriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Find test templates that have specific parameter field
     * Note: Temporarily disabled for H2 compatibility
     */
    // @Query("SELECT t FROM TestTemplate t WHERE JSON_EXTRACT(t.parameters, CONCAT('$.fields[*].name')) LIKE CONCAT('%', :parameterName, '%')")
    // List<TestTemplate> findByParameterExists(@Param("parameterName") String parameterName);
    
    /**
     * Find test templates ordered by name
     */
    List<TestTemplate> findAllByOrderByNameAsc();
    
    /**
     * Find test templates ordered by price
     */
    List<TestTemplate> findAllByOrderByBasePriceAsc();
    
    /**
     * Check if template name exists (for uniqueness validation)
     */
    boolean existsByNameIgnoreCase(String name);
}
