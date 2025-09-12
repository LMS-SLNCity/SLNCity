package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.CreateTestTemplateRequest;
import com.sivalab.laboperations.dto.TestTemplateResponse;
import com.sivalab.laboperations.service.TestTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-templates")
@CrossOrigin(origins = "*")
public class TestTemplateController {
    
    private final TestTemplateService testTemplateService;
    
    @Autowired
    public TestTemplateController(TestTemplateService testTemplateService) {
        this.testTemplateService = testTemplateService;
    }
    
    /**
     * Create a new test template
     * POST /test-templates
     */
    @PostMapping
    public ResponseEntity<TestTemplateResponse> createTestTemplate(@Valid @RequestBody CreateTestTemplateRequest request) {
        try {
            TestTemplateResponse response = testTemplateService.createTestTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw new RuntimeException("Failed to create test template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get test template by ID
     * GET /test-templates/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestTemplateResponse> getTestTemplate(@PathVariable Long id) {
        try {
            TestTemplateResponse response = testTemplateService.getTestTemplate(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw new RuntimeException("Failed to get test template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all test templates
     * GET /test-templates
     */
    @GetMapping
    public ResponseEntity<List<TestTemplateResponse>> getAllTestTemplates() {
        try {
            List<TestTemplateResponse> templates = testTemplateService.getAllTestTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get test templates: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search test templates by name
     * GET /test-templates/search?name=blood
     */
    @GetMapping("/search")
    public ResponseEntity<List<TestTemplateResponse>> searchTestTemplates(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<TestTemplateResponse> templates = testTemplateService.searchTestTemplatesByName(name.trim());
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search test templates: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update test template
     * PUT /test-templates/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestTemplateResponse> updateTestTemplate(@PathVariable Long id,
                                                                  @Valid @RequestBody CreateTestTemplateRequest request) {
        try {
            TestTemplateResponse response = testTemplateService.updateTestTemplate(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw new RuntimeException("Failed to update test template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete test template
     * DELETE /test-templates/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestTemplate(@PathVariable Long id) {
        try {
            testTemplateService.deleteTestTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("being used")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw new RuntimeException("Failed to delete test template: " + e.getMessage(), e);
        }
    }
}
