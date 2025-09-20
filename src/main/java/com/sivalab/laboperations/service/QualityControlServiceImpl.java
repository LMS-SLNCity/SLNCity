package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.dto.QualityControlRequest;
import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.entity.QualityControlResult;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.QualityControlRepository;
import com.sivalab.laboperations.repository.QualityControlResultRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QualityControlServiceImpl implements QualityControlService {

    private final QualityControlRepository qualityControlRepository;
    private final QualityControlResultRepository qualityControlResultRepository;
    private final WestgardService westgardService;
    private final TestTemplateRepository testTemplateRepository;

    public QualityControlServiceImpl(QualityControlRepository qualityControlRepository,
                                     QualityControlResultRepository qualityControlResultRepository,
                                     WestgardService westgardService,
                                     TestTemplateRepository testTemplateRepository) {
        this.qualityControlRepository = qualityControlRepository;
        this.qualityControlResultRepository = qualityControlResultRepository;
        this.westgardService = westgardService;
        this.testTemplateRepository = testTemplateRepository;
    }

    @Override
    public QualityControl createQualityControl(QualityControlRequest qualityControlRequest) {
        TestTemplate testTemplate = testTemplateRepository.findById(qualityControlRequest.getTestTemplateId()).orElse(null);
        if (testTemplate != null) {
            QualityControl qualityControl = new QualityControl();
            qualityControl.setTestTemplate(testTemplate);
            qualityControl.setControlName(qualityControlRequest.getControlName());
            qualityControl.setControlLevel(qualityControlRequest.getControlLevel());
            qualityControl.setFrequency(qualityControlRequest.getFrequency());
            qualityControl.setWestgardRules(qualityControlRequest.getWestgardRules());
            qualityControl.setNextDueDate(calculateNextDueDate(qualityControl.getFrequency()));
            return qualityControlRepository.save(qualityControl);
        }
        return null;
    }

    @Override
    public QualityControl getQualityControlById(Long id) {
        return qualityControlRepository.findById(id).orElse(null);
    }

    @Override
    public List<QualityControl> getAllQualityControls() {
        return qualityControlRepository.findAll();
    }

    @Override
    public QualityControl updateQualityControl(Long id, QualityControlRequest qualityControlRequest) {
        QualityControl existingQualityControl = qualityControlRepository.findById(id).orElse(null);
        if (existingQualityControl != null) {
            TestTemplate testTemplate = testTemplateRepository.findById(qualityControlRequest.getTestTemplateId()).orElse(null);
            if (testTemplate != null) {
                existingQualityControl.setTestTemplate(testTemplate);
                existingQualityControl.setControlName(qualityControlRequest.getControlName());
                existingQualityControl.setControlLevel(qualityControlRequest.getControlLevel());
                existingQualityControl.setFrequency(qualityControlRequest.getFrequency());
                existingQualityControl.setWestgardRules(qualityControlRequest.getWestgardRules());
                existingQualityControl.setNextDueDate(calculateNextDueDate(existingQualityControl.getFrequency()));
                return qualityControlRepository.save(existingQualityControl);
            }
        }
        return null;
    }

    @Override
    public void deleteQualityControl(Long id) {
        qualityControlRepository.deleteById(id);
    }

    @Override
    public QualityControlResult recordQualityControlResult(Long qcId, QualityControlResult result) {
        QualityControl qualityControl = qualityControlRepository.findById(qcId).orElse(null);
        if (qualityControl != null) {
            result.setQualityControl(qualityControl);
            List<QualityControlResult> results = getQualityControlResultsByQcId(qcId);
            results.add(result);
            boolean passed = westgardService.evaluate(results, qualityControl.getWestgardRules());
            result.setPassed(passed);
            qualityControl.setNextDueDate(calculateNextDueDate(qualityControl.getFrequency()));
            qualityControlRepository.save(qualityControl);
            return qualityControlResultRepository.save(result);
        }
        return null;
    }

    @Override
    public List<QualityControlResult> getQualityControlResultsByQcId(Long qcId) {
        return qualityControlResultRepository.findAll().stream()
                .filter(result -> result.getQualityControl().getId().equals(qcId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private LocalDateTime calculateNextDueDate(JsonNode frequency) {
        String type = frequency.get("type").asText();
        if ("DAILY".equalsIgnoreCase(type)) {
            return LocalDateTime.now().plusDays(1);
        } else if ("WEEKLY".equalsIgnoreCase(type)) {
            String dayOfWeekStr = frequency.get("dayOfWeek").asText();
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
            LocalDateTime nextDueDate = LocalDateTime.now().with(dayOfWeek);
            if (nextDueDate.isBefore(LocalDateTime.now())) {
                nextDueDate = nextDueDate.plusWeeks(1);
            }
            return nextDueDate;
        } else if ("MONTHLY".equalsIgnoreCase(type)) {
            int dayOfMonth = frequency.get("dayOfMonth").asInt();
            LocalDateTime nextDueDate = LocalDateTime.now().withDayOfMonth(dayOfMonth);
            if (nextDueDate.isBefore(LocalDateTime.now())) {
                nextDueDate = nextDueDate.plusMonths(1);
            }
            return nextDueDate;
        } else if ("SPECIFIC_DATES".equalsIgnoreCase(type)) {
            JsonNode datesNode = frequency.get("dates");
            List<LocalDate> dates = new ArrayList<>();
            if (datesNode.isArray()) {
                for (JsonNode dateNode : datesNode) {
                    dates.add(LocalDate.parse(dateNode.asText()));
                }
            }
            Collections.sort(dates);
            for (LocalDate date : dates) {
                LocalDateTime nextDueDate = date.atStartOfDay();
                if (nextDueDate.isAfter(LocalDateTime.now())) {
                    return nextDueDate;
                }
            }
            return null;
        }
        return LocalDateTime.now().plusDays(1);
    }
}
