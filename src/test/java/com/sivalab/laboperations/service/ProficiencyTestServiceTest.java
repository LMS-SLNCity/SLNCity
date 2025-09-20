package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.ProficiencyTestRequest;
import com.sivalab.laboperations.entity.ProficiencyTest;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.ProficiencyTestRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProficiencyTestServiceTest {

    @Mock
    private ProficiencyTestRepository proficiencyTestRepository;

    @Mock
    private TestTemplateRepository testTemplateRepository;

    @InjectMocks
    private ProficiencyTestServiceImpl proficiencyTestService;

    @Test
    void shouldCreateProficiencyTest() {
        ProficiencyTestRequest request = new ProficiencyTestRequest();
        request.setTestTemplateId(1L);
        request.setProvider("CAP");

        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setTemplateId(1L);

        when(testTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(proficiencyTestRepository.save(any(ProficiencyTest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProficiencyTest result = proficiencyTestService.createProficiencyTest(request);

        assertThat(result).isNotNull();
        assertThat(result.getProvider()).isEqualTo("CAP");
    }

    @Test
    void shouldGetProficiencyTestById() {
        ProficiencyTest pt = new ProficiencyTest();
        pt.setId(1L);
        pt.setProvider("CAP");

        when(proficiencyTestRepository.findById(1L)).thenReturn(Optional.of(pt));

        ProficiencyTest result = proficiencyTestService.getProficiencyTestById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldGetAllProficiencyTests() {
        ProficiencyTest pt1 = new ProficiencyTest();
        pt1.setId(1L);
        ProficiencyTest pt2 = new ProficiencyTest();
        pt2.setId(2L);
        List<ProficiencyTest> pts = Arrays.asList(pt1, pt2);

        when(proficiencyTestRepository.findAll()).thenReturn(pts);

        List<ProficiencyTest> result = proficiencyTestService.getAllProficiencyTests();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateProficiencyTest() {
        ProficiencyTestRequest request = new ProficiencyTestRequest();
        request.setTestTemplateId(1L);
        request.setProvider("New Provider");

        ProficiencyTest existingPt = new ProficiencyTest();
        existingPt.setId(1L);
        existingPt.setProvider("Old Provider");

        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setTemplateId(1L);

        when(proficiencyTestRepository.findById(1L)).thenReturn(Optional.of(existingPt));
        when(testTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(proficiencyTestRepository.save(any(ProficiencyTest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProficiencyTest result = proficiencyTestService.updateProficiencyTest(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getProvider()).isEqualTo("New Provider");
    }

    @Test
    void shouldDeleteProficiencyTest() {
        proficiencyTestService.deleteProficiencyTest(1L);
        verify(proficiencyTestRepository).deleteById(1L);
    }
}
