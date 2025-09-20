package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.ProficiencyTestRequest;
import com.sivalab.laboperations.entity.ProficiencyTest;
import java.util.List;

public interface ProficiencyTestService {
    ProficiencyTest createProficiencyTest(ProficiencyTestRequest proficiencyTestRequest);
    ProficiencyTest getProficiencyTestById(Long id);
    List<ProficiencyTest> getAllProficiencyTests();
    ProficiencyTest updateProficiencyTest(Long id, ProficiencyTestRequest proficiencyTestRequest);
    void deleteProficiencyTest(Long id);
}
