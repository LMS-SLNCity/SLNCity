package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.QualityIndicator;
import com.sivalab.laboperations.repository.QualityIndicatorRepository;
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
public class QualityIndicatorServiceTest {

    @Mock
    private QualityIndicatorRepository qualityIndicatorRepository;

    @InjectMocks
    private QualityIndicatorServiceImpl qualityIndicatorService;

    @Test
    void shouldCreateQualityIndicator() {
        QualityIndicator qi = new QualityIndicator();
        qi.setName("TAT");
        qi.setIndicatorValue("95%");

        when(qualityIndicatorRepository.save(any(QualityIndicator.class))).thenReturn(qi);

        QualityIndicator result = qualityIndicatorService.createQualityIndicator(qi);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TAT");
    }

    @Test
    void shouldGetQualityIndicatorById() {
        QualityIndicator qi = new QualityIndicator();
        qi.setId(1L);
        qi.setName("TAT");

        when(qualityIndicatorRepository.findById(1L)).thenReturn(Optional.of(qi));

        QualityIndicator result = qualityIndicatorService.getQualityIndicatorById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldGetAllQualityIndicators() {
        QualityIndicator qi1 = new QualityIndicator();
        qi1.setId(1L);
        QualityIndicator qi2 = new QualityIndicator();
        qi2.setId(2L);
        List<QualityIndicator> qis = Arrays.asList(qi1, qi2);

        when(qualityIndicatorRepository.findAll()).thenReturn(qis);

        List<QualityIndicator> result = qualityIndicatorService.getAllQualityIndicators();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateQualityIndicator() {
        QualityIndicator existingQi = new QualityIndicator();
        existingQi.setId(1L);
        existingQi.setName("Old Name");

        QualityIndicator newQi = new QualityIndicator();
        newQi.setName("New Name");

        when(qualityIndicatorRepository.findById(1L)).thenReturn(Optional.of(existingQi));
        when(qualityIndicatorRepository.save(any(QualityIndicator.class))).thenReturn(newQi);

        QualityIndicator result = qualityIndicatorService.updateQualityIndicator(1L, newQi);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteQualityIndicator() {
        qualityIndicatorService.deleteQualityIndicator(1L);
        verify(qualityIndicatorRepository).deleteById(1L);
    }
}
