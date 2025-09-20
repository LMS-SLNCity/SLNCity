package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.ProficiencyTestRequest;
import com.sivalab.laboperations.entity.ProficiencyTest;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.ProficiencyTestRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProficiencyTestServiceImpl implements ProficiencyTestService {

    private final ProficiencyTestRepository proficiencyTestRepository;
    private final TestTemplateRepository testTemplateRepository;

    public ProficiencyTestServiceImpl(ProficiencyTestRepository proficiencyTestRepository, TestTemplateRepository testTemplateRepository) {
        this.proficiencyTestRepository = proficiencyTestRepository;
        this.testTemplateRepository = testTemplateRepository;
    }

    @Override
    public ProficiencyTest createProficiencyTest(ProficiencyTestRequest proficiencyTestRequest) {
        TestTemplate testTemplate = testTemplateRepository.findById(proficiencyTestRequest.getTestTemplateId()).orElse(null);
        if (testTemplate != null) {
            ProficiencyTest proficiencyTest = new ProficiencyTest();
            proficiencyTest.setProvider(proficiencyTestRequest.getProvider());
            proficiencyTest.setTestDate(proficiencyTestRequest.getTestDate());
            proficiencyTest.setTestTemplate(testTemplate);
            proficiencyTest.setResults(proficiencyTestRequest.getResults());
            proficiencyTest.setPassed(proficiencyTestRequest.isPassed());
            return proficiencyTestRepository.save(proficiencyTest);
        }
        return null;
    }

    @Override
    public ProficiencyTest getProficiencyTestById(Long id) {
        return proficiencyTestRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProficiencyTest> getAllProficiencyTests() {
        return proficiencyTestRepository.findAll();
    }

    @Override
    public ProficiencyTest updateProficiencyTest(Long id, ProficiencyTestRequest proficiencyTestRequest) {
        ProficiencyTest existingProficiencyTest = proficiencyTestRepository.findById(id).orElse(null);
        if (existingProficiencyTest != null) {
            TestTemplate testTemplate = testTemplateRepository.findById(proficiencyTestRequest.getTestTemplateId()).orElse(null);
            if (testTemplate != null) {
                existingProficiencyTest.setProvider(proficiencyTestRequest.getProvider());
                existingProficiencyTest.setTestDate(proficiencyTestRequest.getTestDate());
                existingProficiencyTest.setTestTemplate(testTemplate);
                existingProficiencyTest.setResults(proficiencyTestRequest.getResults());
                existingProficiencyTest.setPassed(proficiencyTestRequest.isPassed());
                return proficiencyTestRepository.save(existingProficiencyTest);
            }
        }
        return null;
    }

    @Override
    public void deleteProficiencyTest(Long id) {
        proficiencyTestRepository.deleteById(id);
    }
}
