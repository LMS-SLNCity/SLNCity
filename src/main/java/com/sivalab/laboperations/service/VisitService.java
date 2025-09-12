package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.*;
import com.sivalab.laboperations.entity.Visit;
import com.sivalab.laboperations.entity.VisitStatus;
import com.sivalab.laboperations.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VisitService {
    
    private final VisitRepository visitRepository;
    private final LabTestService labTestService;
    private final BillingService billingService;
    
    @Autowired
    public VisitService(VisitRepository visitRepository, LabTestService labTestService, BillingService billingService) {
        this.visitRepository = visitRepository;
        this.labTestService = labTestService;
        this.billingService = billingService;
    }
    
    /**
     * Create a new visit
     */
    public VisitResponse createVisit(CreateVisitRequest request) {
        Visit visit = new Visit(request.getPatientDetails());
        visit = visitRepository.save(visit);
        return convertToResponse(visit);
    }
    
    /**
     * Get visit by ID
     */
    @Transactional(readOnly = true)
    public VisitResponse getVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));
        return convertToResponse(visit);
    }
    
    /**
     * Get all visits
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getAllVisits() {
        return visitRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get visits by status
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByStatus(VisitStatus status) {
        return visitRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get visits by patient phone
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByPatientPhone(String phone) {
        return visitRepository.findByPatientPhone(phone).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update visit status
     */
    public VisitResponse updateVisitStatus(Long visitId, VisitStatus newStatus) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + visitId));
        
        // Validate status transition
        validateStatusTransition(visit.getStatus(), newStatus);
        
        visit.setStatus(newStatus);
        visit = visitRepository.save(visit);
        return convertToResponse(visit);
    }
    
    /**
     * Delete visit
     */
    public void deleteVisit(Long visitId) {
        if (!visitRepository.existsById(visitId)) {
            throw new RuntimeException("Visit not found with ID: " + visitId);
        }
        visitRepository.deleteById(visitId);
    }
    
    /**
     * Convert Visit entity to VisitResponse DTO
     */
    private VisitResponse convertToResponse(Visit visit) {
        VisitResponse response = new VisitResponse(
                visit.getVisitId(),
                visit.getPatientDetails(),
                visit.getCreatedAt(),
                visit.getStatus()
        );
        
        // Add lab tests if present
        if (!visit.getLabTests().isEmpty()) {
            List<LabTestResponse> labTestResponses = visit.getLabTests().stream()
                    .map(labTestService::convertToResponse)
                    .collect(Collectors.toList());
            response.setLabTests(labTestResponses);
        }
        
        // Add billing if present
        if (visit.getBilling() != null) {
            response.setBilling(billingService.convertToResponse(visit.getBilling()));
        }
        
        return response;
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(VisitStatus currentStatus, VisitStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != VisitStatus.IN_PROGRESS) {
                    throw new RuntimeException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case IN_PROGRESS:
                if (newStatus != VisitStatus.AWAITING_APPROVAL && newStatus != VisitStatus.PENDING) {
                    throw new RuntimeException("Invalid status transition from IN_PROGRESS to " + newStatus);
                }
                break;
            case AWAITING_APPROVAL:
                if (newStatus != VisitStatus.APPROVED && newStatus != VisitStatus.IN_PROGRESS) {
                    throw new RuntimeException("Invalid status transition from AWAITING_APPROVAL to " + newStatus);
                }
                break;
            case APPROVED:
                if (newStatus != VisitStatus.BILLED) {
                    throw new RuntimeException("Invalid status transition from APPROVED to " + newStatus);
                }
                break;
            case BILLED:
                if (newStatus != VisitStatus.COMPLETED) {
                    throw new RuntimeException("Invalid status transition from BILLED to " + newStatus);
                }
                break;
            case COMPLETED:
                throw new RuntimeException("Cannot change status from COMPLETED");
        }
    }
}
