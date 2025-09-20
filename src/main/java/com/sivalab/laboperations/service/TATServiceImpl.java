package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.TAT;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.TATRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TATServiceImpl implements TATService {

    private final TATRepository tatRepository;
    private final TestTemplateRepository testTemplateRepository;

    public TATServiceImpl(TATRepository tatRepository, TestTemplateRepository testTemplateRepository) {
        this.tatRepository = tatRepository;
        this.testTemplateRepository = testTemplateRepository;
    }

    @Override
    public TAT createTAT(Long testTemplateId, int tatValue, TAT.TATUnit tatUnit) {
        TestTemplate testTemplate = testTemplateRepository.findById(testTemplateId)
                .orElseThrow(() -> new RuntimeException("TestTemplate not found"));
        TAT tat = new TAT(null, testTemplate, tatValue, tatUnit);
        return tatRepository.save(tat);
    }

    @Override
    public Optional<TAT> getTATByTestTemplate_TemplateId(Long testTemplateId) {
        return tatRepository.findByTestTemplate_TemplateId(testTemplateId);
    }

    @Override
    public TAT updateTAT(Long id, int tatValue, TAT.TATUnit tatUnit) {
        TAT tat = tatRepository.findById(id).orElseThrow(() -> new RuntimeException("TAT not found"));
        tat.setTatValue(tatValue);
        tat.setTatUnit(tatUnit);
        return tatRepository.save(tat);
    }

    @Override
    public void deleteTAT(Long id) {
        tatRepository.deleteById(id);
    }
}
