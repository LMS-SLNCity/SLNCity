package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.LabReport;
import com.sivalab.laboperations.entity.ReportStatus;
import com.sivalab.laboperations.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    
    /**
     * Find report by ULR number
     */
    Optional<LabReport> findByUlrNumber(String ulrNumber);
    
    /**
     * Find reports by visit ID
     */
    List<LabReport> findByVisitVisitId(Long visitId);
    
    /**
     * Find reports by status
     */
    List<LabReport> findByReportStatus(ReportStatus status);
    
    /**
     * Find reports by type
     */
    List<LabReport> findByReportType(ReportType type);
    
    /**
     * Find reports by visit ID and type
     */
    List<LabReport> findByVisitVisitIdAndReportType(Long visitId, ReportType type);
    
    /**
     * Find reports generated within date range
     */
    @Query("SELECT r FROM LabReport r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
    List<LabReport> findByGeneratedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find reports authorized by specific person
     */
    List<LabReport> findByAuthorizedBy(String authorizedBy);
    
    /**
     * Find reports that need authorization (generated but not authorized)
     */
    @Query("SELECT r FROM LabReport r WHERE r.reportStatus = 'GENERATED' ORDER BY r.generatedAt ASC")
    List<LabReport> findPendingAuthorization();
    
    /**
     * Find reports authorized within date range
     */
    @Query("SELECT r FROM LabReport r WHERE r.authorizedAt BETWEEN :startDate AND :endDate")
    List<LabReport> findByAuthorizedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count reports by status
     */
    long countByReportStatus(ReportStatus status);
    
    /**
     * Count reports generated today
     */
    @Query("SELECT COUNT(r) FROM LabReport r WHERE CAST(r.generatedAt AS date) = CURRENT_DATE")
    long countReportsGeneratedToday();

    /**
     * Count reports by year
     */
    @Query("SELECT COUNT(r) FROM LabReport r WHERE EXTRACT(YEAR FROM r.createdAt) = :year")
    long countReportsByYear(@Param("year") int year);
    
    /**
     * Find latest report for a visit
     */
    @Query("SELECT r FROM LabReport r WHERE r.visit.visitId = :visitId ORDER BY r.createdAt DESC")
    List<LabReport> findLatestReportForVisit(@Param("visitId") Long visitId);
    
    /**
     * Check if ULR number exists
     */
    boolean existsByUlrNumber(String ulrNumber);
    
    /**
     * Find reports that are NABL compliant
     */
    List<LabReport> findByNablCompliant(Boolean nablCompliant);
    
    /**
     * Find reports by template version
     */
    List<LabReport> findByTemplateVersion(String templateVersion);
}
