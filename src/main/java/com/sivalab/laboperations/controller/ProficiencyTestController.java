package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.ProficiencyTestRequest;
import com.sivalab.laboperations.dto.ProficiencyTestResponse;
import com.sivalab.laboperations.entity.ProficiencyTest;
import com.sivalab.laboperations.service.ProficiencyTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proficiency-tests")
public class ProficiencyTestController {

    private final ProficiencyTestService proficiencyTestService;

    public ProficiencyTestController(ProficiencyTestService proficiencyTestService) {
        this.proficiencyTestService = proficiencyTestService;
    }

    @PostMapping
    public ResponseEntity<ProficiencyTestResponse> createProficiencyTest(@RequestBody ProficiencyTestRequest proficiencyTestRequest) {
        ProficiencyTest createdProficiencyTest = proficiencyTestService.createProficiencyTest(proficiencyTestRequest);
        return new ResponseEntity<>(convertToResponse(createdProficiencyTest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProficiencyTestResponse> getProficiencyTestById(@PathVariable Long id) {
        ProficiencyTest proficiencyTest = proficiencyTestService.getProficiencyTestById(id);
        if (proficiencyTest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToResponse(proficiencyTest));
    }

    @GetMapping
    public List<ProficiencyTestResponse> getAllProficiencyTests() {
        return proficiencyTestService.getAllProficiencyTests().stream()
                .map(this::convertToResponse)
                .toList();
    }

    private ProficiencyTestResponse convertToResponse(ProficiencyTest proficiencyTest) {
        ProficiencyTestResponse response = new ProficiencyTestResponse();
        response.setId(proficiencyTest.getId());
        response.setProvider(proficiencyTest.getProvider());
        response.setTestDate(proficiencyTest.getTestDate());
        response.setResults(proficiencyTest.getResults());
        response.setPassed(proficiencyTest.isPassed());
        if (proficiencyTest.getTestTemplate() != null) {
            response.setTestTemplateId(proficiencyTest.getTestTemplate().getTemplateId());
        }
        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProficiencyTestResponse> updateProficiencyTest(@PathVariable Long id, @RequestBody ProficiencyTestRequest proficiencyTestRequest) {
        ProficiencyTest updatedProficiencyTest = proficiencyTestService.updateProficiencyTest(id, proficiencyTestRequest);
        if (updatedProficiencyTest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToResponse(updatedProficiencyTest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProficiencyTest(@PathVariable Long id) {
        ProficiencyTest proficiencyTest = proficiencyTestService.getProficiencyTestById(id);
        if (proficiencyTest == null) {
            return ResponseEntity.notFound().build();
        }
        proficiencyTestService.deleteProficiencyTest(id);
        return ResponseEntity.noContent().build();
    }
}
