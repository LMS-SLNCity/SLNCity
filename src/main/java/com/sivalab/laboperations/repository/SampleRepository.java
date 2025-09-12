package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.Sample;
import com.sivalab.laboperations.entity.SampleStatus;
import com.sivalab.laboperations.entity.SampleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for NABL-compliant sample management
 */
@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    
    /**
     * Find sample by unique sample number
     */
    Optional<Sample> findBySampleNumber(String sampleNumber);
    
    /**
     * Find all samples for a visit
     */
    List<Sample> findByVisitVisitId(Long visitId);
    
    /**
     * Find samples by status
     */
    List<Sample> findByStatus(SampleStatus status);
    
    /**
     * Find samples by type
     */
    List<Sample> findBySampleType(SampleType sampleType);
    
    /**
     * Find samples collected by specific person
     */
    List<Sample> findByCollectedBy(String collectedBy);
    
    /**
     * Find samples received by specific person
     */
    List<Sample> findByReceivedBy(String receivedBy);
    
    /**
     * Find samples processed by specific person
     */
    List<Sample> findByProcessedBy(String processedBy);
    
    /**
     * Find rejected samples
     */
    List<Sample> findByRejectedTrue();
    
    /**
     * Find samples rejected by specific person
     */
    List<Sample> findByRejectedBy(String rejectedBy);
    
    /**
     * Find samples collected within date range
     */
    List<Sample> findByCollectedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find samples received within date range
     */
    List<Sample> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find samples disposed within date range
     */
    List<Sample> findByDisposedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find samples by storage location
     */
    List<Sample> findByStorageLocation(String storageLocation);
    
    /**
     * Find samples requiring disposal (stored beyond retention period)
     */
    @Query("SELECT s FROM Sample s WHERE s.status = 'STORED' AND s.receivedAt < :cutoffDate")
    List<Sample> findSamplesRequiringDisposal(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count samples by status
     */
    @Query("SELECT s.status, COUNT(s) FROM Sample s GROUP BY s.status")
    List<Object[]> countSamplesByStatus();
    
    /**
     * Count samples by type
     */
    @Query("SELECT s.sampleType, COUNT(s) FROM Sample s GROUP BY s.sampleType")
    List<Object[]> countSamplesByType();
    
    /**
     * Find samples with quality issues (rejected or on hold)
     */
    @Query("SELECT s FROM Sample s WHERE s.status IN ('REJECTED', 'ON_HOLD')")
    List<Sample> findSamplesWithQualityIssues();
    
    /**
     * Find samples in processing (active workflow)
     */
    @Query("SELECT s FROM Sample s WHERE s.status IN ('PROCESSING', 'ALIQUOTED', 'IN_ANALYSIS', 'UNDER_REVIEW')")
    List<Sample> findSamplesInProcessing();
    
    /**
     * Find samples by collection site
     */
    List<Sample> findByCollectionSite(String collectionSite);
    
    /**
     * Find samples with insufficient volume
     */
    @Query("SELECT s FROM Sample s WHERE s.volumeReceived < s.volumeRequired")
    List<Sample> findSamplesWithInsufficientVolume();
    
    /**
     * Find samples by container type
     */
    List<Sample> findByContainerType(String containerType);
    
    /**
     * Find samples with specific preservative
     */
    List<Sample> findByPreservative(String preservative);
    
    /**
     * Find samples by disposal method
     */
    List<Sample> findByDisposalMethod(String disposalMethod);
    
    /**
     * Find samples in disposal batch
     */
    List<Sample> findByDisposalBatch(String disposalBatch);
    
    /**
     * Get sample statistics for NABL reporting
     */
    @Query("SELECT " +
           "COUNT(s) as totalSamples, " +
           "COUNT(CASE WHEN s.rejected = true THEN 1 END) as rejectedSamples, " +
           "COUNT(CASE WHEN s.status = 'DISPOSED' THEN 1 END) as disposedSamples, " +
           "COUNT(CASE WHEN s.status IN ('PROCESSING', 'ALIQUOTED', 'IN_ANALYSIS') THEN 1 END) as activeSamples " +
           "FROM Sample s")
    Object[] getSampleStatistics();
    
    /**
     * Find samples requiring attention (overdue processing)
     */
    @Query("SELECT s FROM Sample s WHERE " +
           "s.status IN ('RECEIVED', 'ACCESSIONED', 'PROCESSING') AND " +
           "s.receivedAt < :overdueTime")
    List<Sample> findOverdueSamples(@Param("overdueTime") LocalDateTime overdueTime);
    
    /**
     * Find samples by receipt condition
     */
    List<Sample> findByReceiptCondition(String receiptCondition);
    
    /**
     * Find samples with temperature deviations
     */
    @Query("SELECT s FROM Sample s WHERE " +
           "(s.receiptTemperature IS NOT NULL AND (s.receiptTemperature < 2 OR s.receiptTemperature > 8)) OR " +
           "(s.storageTemperature IS NOT NULL AND (s.storageTemperature < 2 OR s.storageTemperature > 8))")
    List<Sample> findSamplesWithTemperatureDeviations();
    
    /**
     * Check if sample number exists
     */
    boolean existsBySampleNumber(String sampleNumber);
    
    /**
     * Find latest sample for a visit
     */
    @Query("SELECT s FROM Sample s WHERE s.visit.visitId = :visitId ORDER BY s.collectedAt DESC")
    List<Sample> findLatestSampleForVisit(@Param("visitId") Long visitId);
    
    /**
     * Find samples requiring review
     */
    @Query("SELECT s FROM Sample s WHERE s.status = 'UNDER_REVIEW'")
    List<Sample> findSamplesRequiringReview();
    
    /**
     * Find samples by multiple statuses
     */
    List<Sample> findByStatusIn(List<SampleStatus> statuses);
    
    /**
     * Count samples collected today
     */
    @Query("SELECT COUNT(s) FROM Sample s WHERE CAST(s.collectedAt AS date) = CURRENT_DATE")
    Long countSamplesCollectedToday();

    /**
     * Count samples received today
     */
    @Query("SELECT COUNT(s) FROM Sample s WHERE CAST(s.receivedAt AS date) = CURRENT_DATE")
    Long countSamplesReceivedToday();

    /**
     * Count samples processed today
     */
    @Query("SELECT COUNT(s) FROM Sample s WHERE CAST(s.processingCompletedAt AS date) = CURRENT_DATE")
    Long countSamplesProcessedToday();
}
