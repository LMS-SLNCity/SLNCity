package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.dto.QualityControlRequest;
import com.sivalab.laboperations.dto.QualityControlResponse;
import com.sivalab.laboperations.dto.QualityControlResultResponse;
import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.entity.QualityControlResult;
import com.sivalab.laboperations.service.QualityControlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-controls")
public class QualityControlController {

    private final QualityControlService qualityControlService;

    public QualityControlController(QualityControlService qualityControlService) {
        this.qualityControlService = qualityControlService;
    }

    @PostMapping
    public ResponseEntity<QualityControlResponse> createQualityControl(@RequestBody QualityControlRequest qualityControlRequest) {
        QualityControl createdQualityControl = qualityControlService.createQualityControl(qualityControlRequest);
        return new ResponseEntity<>(convertToResponse(createdQualityControl), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualityControlResponse> getQualityControlById(@PathVariable Long id) {
        QualityControl qualityControl = qualityControlService.getQualityControlById(id);
        if (qualityControl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToResponse(qualityControl));
    }

    @GetMapping
    public List<QualityControlResponse> getAllQualityControls() {
        return qualityControlService.getAllQualityControls().stream()
                .map(this::convertToResponse)
                .toList();
    }

    private QualityControlResponse convertToResponse(QualityControl qualityControl) {
        QualityControlResponse response = new QualityControlResponse();
        response.setId(qualityControl.getId());
        response.setControlName(qualityControl.getControlName());
        response.setControlLevel(qualityControl.getControlLevel());
        response.setFrequency(qualityControl.getFrequency());
        response.setWestgardRules(qualityControl.getWestgardRules());
        response.setNextDueDate(qualityControl.getNextDueDate());
        if (qualityControl.getTestTemplate() != null) {
            response.setTestTemplateId(qualityControl.getTestTemplate().getTemplateId());
        }
        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualityControlResponse> updateQualityControl(@PathVariable Long id, @RequestBody QualityControlRequest qualityControlRequest) {
        QualityControl updatedQualityControl = qualityControlService.updateQualityControl(id, qualityControlRequest);
        return ResponseEntity.ok(convertToResponse(updatedQualityControl));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQualityControl(@PathVariable Long id) {
        QualityControl qualityControl = qualityControlService.getQualityControlById(id);
        if (qualityControl == null) {
            return ResponseEntity.notFound().build();
        }
        qualityControlService.deleteQualityControl(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{qcId}/results")
    public ResponseEntity<QualityControlResultResponse> recordQualityControlResult(@PathVariable Long qcId, @RequestBody QualityControlResult result) {
        QualityControlResult recordedResult = qualityControlService.recordQualityControlResult(qcId, result);
        return new ResponseEntity<>(convertToResponse(recordedResult), HttpStatus.CREATED);
    }

    @GetMapping("/{qcId}/results")
    public List<QualityControlResultResponse> getQualityControlResultsByQcId(@PathVariable Long qcId) {
        return qualityControlService.getQualityControlResultsByQcId(qcId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    private QualityControlResultResponse convertToResponse(QualityControlResult result) {
        QualityControlResultResponse response = new QualityControlResultResponse();
        response.setId(result.getId());
        response.setResults(result.getResults());
        response.setPassed(result.isPassed());
        response.setTestedAt(result.getTestedAt());
        if (result.getQualityControl() != null) {
            response.setQualityControlId(result.getQualityControl().getId());
        }
        return response;
    }
}
