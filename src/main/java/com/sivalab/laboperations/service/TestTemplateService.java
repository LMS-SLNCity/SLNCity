package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.CreateTestTemplateRequest;
import com.sivalab.laboperations.dto.TestTemplateResponse;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestTemplateService {
    
    private final TestTemplateRepository testTemplateRepository;
    
    @Autowired
    public TestTemplateService(TestTemplateRepository testTemplateRepository) {
        this.testTemplateRepository = testTemplateRepository;
    }
    
    /**
     * Create a new test template
     */
    public TestTemplateResponse createTestTemplate(CreateTestTemplateRequest request) {

        if(request.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than 0");
        }   

        // Check if template with same name already exists
        if (testTemplateRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Test template with name '" + request.getName() + "' already exists");
        }
        
        TestTemplate template = new TestTemplate(
                request.getName(),
                request.getDescription(),
                request.getParameters(),
                request.getBasePrice()
        );
        
        template = testTemplateRepository.save(template);
        return convertToResponse(template);
    }
    
    /**
     * Get test template by ID
     */
    @Transactional(readOnly = true)
    public TestTemplateResponse getTestTemplate(Long templateId) {
        TestTemplate template = testTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Test template not found with ID: " + templateId));
        return convertToResponse(template);
    }
    
    /**
     * Get all test templates
     */
    @Transactional(readOnly = true)
    public List<TestTemplateResponse> getAllTestTemplates() {
        return testTemplateRepository.findAllByOrderByNameAsc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Search test templates by name
     */
    @Transactional(readOnly = true)
    public List<TestTemplateResponse> searchTestTemplatesByName(String name) {
        return testTemplateRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update test template
     */
    public TestTemplateResponse updateTestTemplate(Long templateId, CreateTestTemplateRequest request) {
        TestTemplate template = testTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Test template not found with ID: " + templateId));
        
        // Check if another template with same name exists
        testTemplateRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(existingTemplate -> {
                    if (!existingTemplate.getTemplateId().equals(templateId)) {
                        throw new RuntimeException("Test template with name '" + request.getName() + "' already exists");
                    }
                });
        
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setParameters(request.getParameters());
        template.setBasePrice(request.getBasePrice());
        
        template = testTemplateRepository.save(template);
        return convertToResponse(template);
    }
    
    /**
     * Delete test template
     */
    public void deleteTestTemplate(Long templateId) {
        TestTemplate template = testTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Test template not found with ID: " + templateId));
        
        // Check if template is being used in any lab tests
        if (!template.getLabTests().isEmpty()) {
            throw new RuntimeException("Cannot delete test template that is being used in lab tests");
        }
        
        testTemplateRepository.delete(template);
    }
    
    /**
     * Convert TestTemplate entity to TestTemplateResponse DTO
     */
    public TestTemplateResponse convertToResponse(TestTemplate template) {
        return new TestTemplateResponse(
                template.getTemplateId(),
                template.getName(),
                template.getDescription(),
                template.getParameters(),
                template.getBasePrice(),
                template.getCreatedAt()
        );
    }
}
