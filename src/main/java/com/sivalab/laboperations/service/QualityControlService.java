package com.sivalab.laboperations.service;

import com.sivalab.laboperations.dto.QualityControlRequest;
import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.entity.QualityControlResult;
import java.util.List;

public interface QualityControlService {
    QualityControl createQualityControl(QualityControlRequest qualityControlRequest);
    QualityControl getQualityControlById(Long id);
    List<QualityControl> getAllQualityControls();
    QualityControl updateQualityControl(Long id, QualityControlRequest qualityControlRequest);
    void deleteQualityControl(Long id);

    QualityControlResult recordQualityControlResult(Long qcId, QualityControlResult result);
    List<QualityControlResult> getQualityControlResultsByQcId(Long qcId);
}
