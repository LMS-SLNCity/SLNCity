package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.LabTest;
import com.sivalab.laboperations.entity.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    
    /**
     * Find lab tests by visit ID
     */
    List<LabTest> findByVisitVisitId(Long visitId);
    
    /**
     * Find lab tests by visit ID and test ID
     */
    Optional<LabTest> findByVisitVisitIdAndTestId(Long visitId, Long testId);
    
    /**
     * Find lab tests by status
     */
    List<LabTest> findByStatus(TestStatus status);

    /**
     * Count lab tests by status
     */
    long countByStatus(TestStatus status);

    /**
     * Count approved lab tests
     */
    long countByApprovedTrue();
    
    /**
     * Find lab tests by approval status
     */
    List<LabTest> findByApproved(Boolean approved);
    
    /**
     * Find lab tests by test template
     */
    List<LabTest> findByTestTemplateTemplateId(Long templateId);
    
    /**
     * Find lab tests approved by specific person
     */
    List<LabTest> findByApprovedBy(String approvedBy);
    
    /**
     * Find lab tests approved within date range
     */
    List<LabTest> findByApprovedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count tests that are completed but not yet approved for a visit
     */
    @Query("SELECT COUNT(lt) FROM LabTest lt WHERE lt.visit.visitId = :visitId AND lt.status = 'COMPLETED' AND lt.approved = false")
    long countPendingTestsForVisit(@Param("visitId") Long visitId);

    /**
     * Count tests that are not yet completed for a visit (PENDING or IN_PROGRESS)
     */
    @Query("SELECT COUNT(lt) FROM LabTest lt WHERE lt.visit.visitId = :visitId AND lt.status IN ('PENDING', 'IN_PROGRESS')")
    long countIncompleteTestsForVisit(@Param("visitId") Long visitId);

    /**
     * Find tests that need approval (completed but not approved)
     */
    @Query("SELECT lt FROM LabTest lt WHERE lt.status = 'COMPLETED' AND lt.approved = false")
    List<LabTest> findTestsNeedingApproval();
    
    /**
     * Calculate total price for visit tests
     */
    @Query("SELECT COALESCE(SUM(lt.price), 0) FROM LabTest lt WHERE lt.visit.visitId = :visitId")
    java.math.BigDecimal calculateTotalPriceForVisit(@Param("visitId") Long visitId);
    
    /**
     * Find tests with results containing specific value
     * Note: Temporarily disabled for H2 compatibility
     */
    // @Query("SELECT lt FROM LabTest lt WHERE JSON_EXTRACT(lt.results, CONCAT('$.', :key)) IS NOT NULL")
    // List<LabTest> findTestsWithResultKey(@Param("key") String key);

    /**
     * Find tests that don't have samples collected yet
     */
    @Query("SELECT lt FROM LabTest lt WHERE lt.sample IS NULL AND lt.status = 'PENDING'")
    List<LabTest> findTestsWithoutSamples();
}
