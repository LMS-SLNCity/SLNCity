package com.sivalab.laboperations.repository;

import com.sivalab.laboperations.entity.Visit;
import com.sivalab.laboperations.entity.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    
    /**
     * Find visits by status
     */
    List<Visit> findByStatus(VisitStatus status);
    
    /**
     * Find visits by patient phone number using JSON query
     * Note: Simplified for H2 compatibility - will be enhanced for PostgreSQL
     */
    // @Query("SELECT v FROM Visit v WHERE JSON_EXTRACT(v.patientDetails, '$.phone') = :phone")
    // List<Visit> findByPatientPhone(@Param("phone") String phone);

    /**
     * Find visits by patient name using JSON query (case-insensitive)
     * Note: Simplified for H2 compatibility - will be enhanced for PostgreSQL
     */
    // @Query("SELECT v FROM Visit v WHERE LOWER(JSON_EXTRACT(v.patientDetails, '$.name')) LIKE LOWER(CONCAT('%', :name, '%'))")
    // List<Visit> findByPatientNameContaining(@Param("name") String name);
    
    /**
     * Find visits created between dates
     */
    List<Visit> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find visits by status and created date range
     */
    List<Visit> findByStatusAndCreatedAtBetween(VisitStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count visits by status
     */
    long countByStatus(VisitStatus status);
    
    /**
     * Find visits with pending lab tests
     */
    @Query("SELECT DISTINCT v FROM Visit v JOIN v.labTests lt WHERE lt.approved = false")
    List<Visit> findVisitsWithPendingTests();
    
    /**
     * Find visits ready for billing (all tests approved but not billed)
     */
    @Query("SELECT v FROM Visit v WHERE v.status = 'APPROVED' AND v.billing IS NULL")
    List<Visit> findVisitsReadyForBilling();
}
