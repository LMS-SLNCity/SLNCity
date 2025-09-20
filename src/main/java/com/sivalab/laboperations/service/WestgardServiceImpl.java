package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sivalab.laboperations.entity.QualityControlResult;
import com.sivalab.laboperations.entity.TestTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class WestgardServiceImpl implements WestgardService {

    @Override
    public boolean evaluate(List<QualityControlResult> results, String rules) {
        if (results == null || results.isEmpty()) {
            return true;
        }
        String[] ruleSet = rules.split(",");
        for (String rule : ruleSet) {
            if ("1-3s".equalsIgnoreCase(rule)) {
                if (is13sViolated(results)) {
                    return false;
                }
            } else if ("2-2s".equalsIgnoreCase(rule)) {
                if (is22sViolated(results)) {
                    return false;
                }
            } else if ("R-4s".equalsIgnoreCase(rule)) {
                if (isR4sViolated(results)) {
                    return false;
                }
            } else if ("4-1s".equalsIgnoreCase(rule)) {
                if (is41sViolated(results)) {
                    return false;
                }
            } else if ("10-x".equalsIgnoreCase(rule)) {
                if (is10xViolated(results)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean is13sViolated(List<QualityControlResult> results) {
        QualityControlResult lastResult = results.get(results.size() - 1);
        TestTemplate testTemplate = lastResult.getQualityControl().getTestTemplate();
        JsonNode parameters = testTemplate.getParameters();
        JsonNode qcResults = lastResult.getResults();

        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String paramName = entry.getKey();
            JsonNode paramValues = entry.getValue();
            double mean = paramValues.get("mean").asDouble();
            double stdDev = paramValues.get("stdDev").asDouble();
            double value = qcResults.get(paramName).asDouble();
            if (Math.abs(value - mean) > 3 * stdDev) {
                return true;
            }
        }
        return false;
    }

    private boolean is22sViolated(List<QualityControlResult> results) {
        if (results.size() < 2) {
            return false;
        }
        QualityControlResult lastResult = results.get(results.size() - 1);
        QualityControlResult secondLastResult = results.get(results.size() - 2);
        TestTemplate testTemplate = lastResult.getQualityControl().getTestTemplate();
        JsonNode parameters = testTemplate.getParameters();
        JsonNode lastQcResults = lastResult.getResults();
        JsonNode secondLastQcResults = secondLastResult.getResults();

        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String paramName = entry.getKey();
            JsonNode paramValues = entry.getValue();
            double mean = paramValues.get("mean").asDouble();
            double stdDev = paramValues.get("stdDev").asDouble();
            double lastValue = lastQcResults.get(paramName).asDouble();
            double secondLastValue = secondLastQcResults.get(paramName).asDouble();

            if ((lastValue > mean + 2 * stdDev && secondLastValue > mean + 2 * stdDev) ||
                (lastValue < mean - 2 * stdDev && secondLastValue < mean - 2 * stdDev)) {
                return true;
            }
        }
        return false;
    }

    private boolean isR4sViolated(List<QualityControlResult> results) {
        if (results.size() < 2) {
            return false;
        }
        QualityControlResult lastResult = results.get(results.size() - 1);
        QualityControlResult secondLastResult = results.get(results.size() - 2);
        TestTemplate testTemplate = lastResult.getQualityControl().getTestTemplate();
        JsonNode parameters = testTemplate.getParameters();
        JsonNode lastQcResults = lastResult.getResults();
        JsonNode secondLastQcResults = secondLastResult.getResults();

        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String paramName = entry.getKey();
            JsonNode paramValues = entry.getValue();
            double stdDev = paramValues.get("stdDev").asDouble();
            double lastValue = lastQcResults.get(paramName).asDouble();
            double secondLastValue = secondLastQcResults.get(paramName).asDouble();

            if (Math.abs(lastValue - secondLastValue) > 4 * stdDev) {
                return true;
            }
        }
        return false;
    }

    private boolean is41sViolated(List<QualityControlResult> results) {
        if (results.size() < 4) {
            return false;
        }
        List<QualityControlResult> lastFourResults = results.subList(results.size() - 4, results.size());
        TestTemplate testTemplate = lastFourResults.get(0).getQualityControl().getTestTemplate();
        JsonNode parameters = testTemplate.getParameters();

        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String paramName = entry.getKey();
            JsonNode paramValues = entry.getValue();
            double mean = paramValues.get("mean").asDouble();
            double stdDev = paramValues.get("stdDev").asDouble();

            int countAbove = 0;
            int countBelow = 0;

            for (QualityControlResult result : lastFourResults) {
                double value = result.getResults().get(paramName).asDouble();
                if (value > mean + stdDev) {
                    countAbove++;
                } else if (value < mean - stdDev) {
                    countBelow++;
                }
            }

            if (countAbove == 4 || countBelow == 4) {
                return true;
            }
        }
        return false;
    }

    private boolean is10xViolated(List<QualityControlResult> results) {
        if (results.size() < 10) {
            return false;
        }
        List<QualityControlResult> lastTenResults = results.subList(results.size() - 10, results.size());
        TestTemplate testTemplate = lastTenResults.get(0).getQualityControl().getTestTemplate();
        JsonNode parameters = testTemplate.getParameters();

        Iterator<Map.Entry<String, JsonNode>> fields = parameters.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String paramName = entry.getKey();
            JsonNode paramValues = entry.getValue();
            double mean = paramValues.get("mean").asDouble();

            int countAbove = 0;
            int countBelow = 0;

            for (QualityControlResult result : lastTenResults) {
                double value = result.getResults().get(paramName).asDouble();
                if (value > mean) {
                    countAbove++;
                } else if (value < mean) {
                    countBelow++;
                }
            }

            if (countAbove == 10 || countBelow == 10) {
                return true;
            }
        }
        return false;
    }
}
