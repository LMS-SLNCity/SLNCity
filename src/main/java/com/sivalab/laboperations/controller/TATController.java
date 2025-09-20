package com.sivalab.laboperations.controller;

import com.sivalab.laboperations.entity.TAT;
import com.sivalab.laboperations.service.TATService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tats")
public class TATController {

    private final TATService tatService;

    public TATController(TATService tatService) {
        this.tatService = tatService;
    }

    @PostMapping
    public ResponseEntity<TAT> createTAT(@RequestBody TATRequestDTO tatRequestDTO) {
        TAT createdTAT = tatService.createTAT(
                tatRequestDTO.getTestTemplateId(),
                tatRequestDTO.getTatValue(),
                tatRequestDTO.getTatUnit()
        );
        return new ResponseEntity<>(createdTAT, HttpStatus.CREATED);
    }

    @GetMapping("/test-template/{testTemplateId}")
    public ResponseEntity<TAT> getTATByTestTemplateId(@PathVariable Long testTemplateId) {
        Optional<TAT> tat = tatService.getTATByTestTemplate_TemplateId(testTemplateId);
        return tat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TAT> updateTAT(@PathVariable Long id, @RequestBody TATRequestDTO tatRequestDTO) {
        TAT updatedTAT = tatService.updateTAT(
                id,
                tatRequestDTO.getTatValue(),
                tatRequestDTO.getTatUnit()
        );
        return ResponseEntity.ok(updatedTAT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTAT(@PathVariable Long id) {
        tatService.deleteTAT(id);
        return ResponseEntity.noContent().build();
    }
}

class TATRequestDTO {
    private Long testTemplateId;
    private int tatValue;
    private TAT.TATUnit tatUnit;

    // Getters and Setters
    public Long getTestTemplateId() {
        return testTemplateId;
    }

    public void setTestTemplateId(Long testTemplateId) {
        this.testTemplateId = testTemplateId;
    }

    public int getTatValue() {
        return tatValue;
    }

    public void setTatValue(int tatValue) {
        this.tatValue = tatValue;
    }

    public TAT.TATUnit getTatUnit() {
        return tatUnit;
    }

    public void setTatUnit(TAT.TATUnit tatUnit) {
        this.tatUnit = tatUnit;
    }
}
