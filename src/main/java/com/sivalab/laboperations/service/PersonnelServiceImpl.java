package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.Personnel;
import com.sivalab.laboperations.repo.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonnelServiceImpl implements PersonnelService {

    private final PersonnelRepository personnelRepository;

    @Autowired
    public PersonnelServiceImpl(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    @Override
    public Personnel save(Personnel personnel) {
        return personnelRepository.save(personnel);
    }

    @Override
    public Optional<Personnel> findById(Long id) {
        return personnelRepository.findById(id);
    }

    @Override
    public List<Personnel> findAll() {
        return personnelRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        personnelRepository.deleteById(id);
    }
}
