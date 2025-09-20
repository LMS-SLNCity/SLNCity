package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.QualityIndicator;
import com.sivalab.laboperations.repository.QualityIndicatorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QualityIndicatorServiceImpl implements QualityIndicatorService {

    private final QualityIndicatorRepository qualityIndicatorRepository;

    public QualityIndicatorServiceImpl(QualityIndicatorRepository qualityIndicatorRepository) {
        this.qualityIndicatorRepository = qualityIndicatorRepository;
    }

    @Override
    public QualityIndicator createQualityIndicator(QualityIndicator qualityIndicator) {
        return qualityIndicatorRepository.save(qualityIndicator);
    }

    @Override
    public QualityIndicator getQualityIndicatorById(Long id) {
        return qualityIndicatorRepository.findById(id).orElse(null);
    }

    @Override
    public List<QualityIndicator> getAllQualityIndicators() {
        return qualityIndicatorRepository.findAll();
    }

    @Override
    public QualityIndicator updateQualityIndicator(Long id, QualityIndicator qualityIndicator) {
        QualityIndicator existingQualityIndicator = qualityIndicatorRepository.findById(id).orElse(null);
        if (existingQualityIndicator != null) {
            existingQualityIndicator.setName(qualityIndicator.getName());
            existingQualityIndicator.setIndicatorValue(qualityIndicator.getIndicatorValue());
            existingQualityIndicator.setRecordedAt(qualityIndicator.getRecordedAt());
            return qualityIndicatorRepository.save(existingQualityIndicator);
        }
        return null;
    }

    @Override
    public void deleteQualityIndicator(Long id) {
        qualityIndicatorRepository.deleteById(id);
    }
}
