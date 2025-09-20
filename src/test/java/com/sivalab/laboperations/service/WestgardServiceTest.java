package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalab.laboperations.entity.QualityControl;
import com.sivalab.laboperations.entity.QualityControlResult;
import com.sivalab.laboperations.entity.TestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WestgardServiceTest {

    private WestgardService westgardService = new WestgardServiceImpl();
    private TestTemplate testTemplate;
    private QualityControl qc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        testTemplate = new TestTemplate();
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Double> param1Values = new HashMap<>();
        param1Values.put("mean", 10.0);
        param1Values.put("stdDev", 1.0);
        parameters.put("param1", param1Values);
        testTemplate.setParameters(objectMapper.valueToTree(parameters));

        qc = new QualityControl();
        qc.setTestTemplate(testTemplate);
    }

    private QualityControlResult createQcResult(double value) {
        QualityControlResult result = new QualityControlResult();
        result.setQualityControl(qc);
        Map<String, Double> results = new HashMap<>();
        results.put("param1", value);
        result.setResults(objectMapper.valueToTree(results));
        return result;
    }

    @Test
    void shouldViolate13sRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        resultList.add(createQcResult(14.0));
        boolean passed = westgardService.evaluate(resultList, "1-3s");
        assertThat(passed).isFalse();
    }

    @Test
    void shouldNotViolate13sRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        resultList.add(createQcResult(12.0));
        boolean passed = westgardService.evaluate(resultList, "1-3s");
        assertThat(passed).isTrue();
    }

    @Test
    void shouldViolate22sRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        resultList.add(createQcResult(12.1));
        resultList.add(createQcResult(12.2));
        boolean passed = westgardService.evaluate(resultList, "2-2s");
        assertThat(passed).isFalse();
    }

    @Test
    void shouldViolateR4sRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        resultList.add(createQcResult(7.9));
        resultList.add(createQcResult(12.1));
        boolean passed = westgardService.evaluate(resultList, "R-4s");
        assertThat(passed).isFalse();
    }

    @Test
    void shouldViolate41sRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        resultList.add(createQcResult(11.1));
        resultList.add(createQcResult(11.2));
        resultList.add(createQcResult(11.3));
        resultList.add(createQcResult(11.4));
        boolean passed = westgardService.evaluate(resultList, "4-1s");
        assertThat(passed).isFalse();
    }

    @Test
    void shouldViolate10xRule() {
        List<QualityControlResult> resultList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            resultList.add(createQcResult(10.1 + i * 0.1));
        }
        boolean passed = westgardService.evaluate(resultList, "10-x");
        assertThat(passed).isFalse();
    }
}
