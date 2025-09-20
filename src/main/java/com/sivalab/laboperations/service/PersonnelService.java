package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.Personnel;
import java.util.List;
import java.util.Optional;

public interface PersonnelService {
    Personnel save(Personnel personnel);
    Optional<Personnel> findById(Long id);
    List<Personnel> findAll();
    void deleteById(Long id);
}
