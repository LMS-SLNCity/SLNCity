package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalab.laboperations.dto.QualityControlRequest;
import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.entity.QualityControlResult;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.QualityControlRepository;
import com.sivalab.laboperations.repository.QualityControlResultRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QualityControlServiceTest {

    @Mock
    private QualityControlRepository qualityControlRepository;

    @Mock
    private QualityControlResultRepository qualityControlResultRepository;

    @Mock
    private TestTemplateRepository testTemplateRepository;

    @Mock
    private WestgardService westgardService;

    @InjectMocks
    private QualityControlServiceImpl qualityControlService;

    @Test
    void shouldCreateQualityControl() {
        QualityControlRequest request = new QualityControlRequest();
        request.setTestTemplateId(1L);
        request.setControlName("Test QC");
        request.setControlLevel(1);
        request.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        request.setFrequency(new ObjectMapper().valueToTree(frequency));

        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setTemplateId(1L);

        when(testTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(qualityControlRepository.save(any(QualityControl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QualityControl result = qualityControlService.createQualityControl(request);

        assertThat(result).isNotNull();
        assertThat(result.getControlName()).isEqualTo("Test QC");
    }

    @Test
    void shouldGetQualityControlById() {
        QualityControl qc = new QualityControl();
        qc.setId(1L);
        qc.setControlName("Test QC");

        when(qualityControlRepository.findById(1L)).thenReturn(Optional.of(qc));

        QualityControl result = qualityControlService.getQualityControlById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldGetAllQualityControls() {
        QualityControl qc1 = new QualityControl();
        qc1.setId(1L);
        QualityControl qc2 = new QualityControl();
        qc2.setId(2L);
        List<QualityControl> qcs = Arrays.asList(qc1, qc2);

        when(qualityControlRepository.findAll()).thenReturn(qcs);

        List<QualityControl> result = qualityControlService.getAllQualityControls();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateQualityControl() {
        QualityControlRequest request = new QualityControlRequest();
        request.setTestTemplateId(1L);
        request.setControlName("Updated Test QC");
        request.setControlLevel(1);
        request.setWestgardRules("1-3s,2-2s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        request.setFrequency(new ObjectMapper().valueToTree(frequency));

        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setTemplateId(1L);

        QualityControl existingQc = new QualityControl();
        existingQc.setId(1L);
        existingQc.setControlName("Old Test QC");

        when(testTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(qualityControlRepository.findById(1L)).thenReturn(Optional.of(existingQc));
        when(qualityControlRepository.save(any(QualityControl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QualityControl result = qualityControlService.updateQualityControl(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getControlName()).isEqualTo("Updated Test QC");
    }

    @Test
    void shouldDeleteQualityControl() {
        qualityControlService.deleteQualityControl(1L);
        verify(qualityControlRepository).deleteById(1L);
    }

    @Test
    void shouldRecordQualityControlResult() {
        QualityControl qc = new QualityControl();
        qc.setId(1L);
        qc.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qc.setFrequency(new ObjectMapper().valueToTree(frequency));

        QualityControlResult result = new QualityControlResult();
        result.setResults(new ObjectMapper().createObjectNode());

        when(qualityControlRepository.findById(1L)).thenReturn(Optional.of(qc));
        when(qualityControlResultRepository.save(any(QualityControlResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(westgardService.evaluate(any(), any())).thenReturn(true);

        QualityControlResult recordedResult = qualityControlService.recordQualityControlResult(1L, result);

        assertThat(recordedResult).isNotNull();
        assertThat(recordedResult.isPassed()).isTrue();
    }

    @Test
    void shouldGetQualityControlResultsByQcId() {
        QualityControl qc = new QualityControl();
        qc.setId(1L);

        QualityControlResult result1 = new QualityControlResult();
        result1.setId(1L);
        result1.setQualityControl(qc);

        QualityControlResult result2 = new QualityControlResult();
        result2.setId(2L);
        result2.setQualityControl(qc);

        List<QualityControlResult> results = Arrays.asList(result1, result2);

        when(qualityControlResultRepository.findAll()).thenReturn(results);

        List<QualityControlResult> result = qualityControlService.getQualityControlResultsByQcId(1L);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }
}
