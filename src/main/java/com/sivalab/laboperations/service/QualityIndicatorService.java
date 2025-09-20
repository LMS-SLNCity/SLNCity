package com.sivalab.laboperations.service;

import com.sivalab.laboperations.entity.QualityIndicator;
import java.util.List;

public interface QualityIndicatorService {
    QualityIndicator createQualityIndicator(QualityIndicator qualityIndicator);
    QualityIndicator getQualityIndicatorById(Long id);
    List<QualityIndicator> getAllQualityIndicators();
    QualityIndicator updateQualityIndicator(Long id, QualityIndicator qualityIndicator);
    void deleteQualityIndicator(Long id);
}
