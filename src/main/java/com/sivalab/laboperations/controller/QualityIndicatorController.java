package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.QualityIndicator;
import com.sivalab.laboperations.service.QualityIndicatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-indicators")
public class QualityIndicatorController {

    private final QualityIndicatorService qualityIndicatorService;

    public QualityIndicatorController(QualityIndicatorService qualityIndicatorService) {
        this.qualityIndicatorService = qualityIndicatorService;
    }

    @PostMapping
    public ResponseEntity<QualityIndicator> createQualityIndicator(@RequestBody QualityIndicator qualityIndicator) {
        QualityIndicator createdQualityIndicator = qualityIndicatorService.createQualityIndicator(qualityIndicator);
        return new ResponseEntity<>(createdQualityIndicator, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualityIndicator> getQualityIndicatorById(@PathVariable Long id) {
        QualityIndicator qualityIndicator = qualityIndicatorService.getQualityIndicatorById(id);
        return ResponseEntity.ok(qualityIndicator);
    }

    @GetMapping
    public List<QualityIndicator> getAllQualityIndicators() {
        return qualityIndicatorService.getAllQualityIndicators();
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualityIndicator> updateQualityIndicator(@PathVariable Long id, @RequestBody QualityIndicator qualityIndicator) {
        QualityIndicator updatedQualityIndicator = qualityIndicatorService.updateQualityIndicator(id, qualityIndicator);
        return ResponseEntity.ok(updatedQualityIndicator);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQualityIndicator(@PathVariable Long id) {
        qualityIndicatorService.deleteQualityIndicator(id);
        return ResponseEntity.noContent().build();
    }
}
