package com.sivalab.laboperations.web;

import com.sivalab.laboperations.entity.Personnel;
import com.sivalab.laboperations.service.PersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    @Autowired
    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @PostMapping
    public ResponseEntity<Personnel> createPersonnel(@RequestBody Personnel personnel) {
        Personnel savedPersonnel = personnelService.save(personnel);
        return new ResponseEntity<>(savedPersonnel, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personnel> getPersonnelById(@PathVariable Long id) {
        return personnelService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Personnel> getAllPersonnel() {
        return personnelService.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personnel> updatePersonnel(@PathVariable Long id, @RequestBody Personnel personnel) {
        return personnelService.findById(id)
                .map(existingPersonnel -> {
                    personnel.setId(id);
                    Personnel updatedPersonnel = personnelService.save(personnel);
                    return ResponseEntity.ok(updatedPersonnel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersonnel(@PathVariable Long id) {
        personnelService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
