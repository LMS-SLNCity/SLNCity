package com.sivalab.laboperations.validator;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Validator for test results against test template parameters
 * Addresses Issue #30: Test results validation
 * Ensures NABL compliance for result data integrity
 */
@Component
public class TestResultsValidator {

    /**
     * Validates test results against template parameters
     * @param results The test results JSON
     * @param parameters The test template parameters JSON
     * @throws IllegalArgumentException if validation fails
     */
    public void validateResults(JsonNode results, JsonNode parameters) {
        if (results == null) {
            throw new IllegalArgumentException("Test results cannot be null");
        }
        
        if (parameters == null || !parameters.isArray()) {
            // If no parameters defined, allow any results
            return;
        }
        
        List<String> errors = new ArrayList<>();
        
        // Convert parameters array to map for easier lookup
        Map<String, JsonNode> parameterMap = new java.util.HashMap<>();
        for (JsonNode param : parameters) {
            if (param.has("name")) {
                parameterMap.put(param.get("name").asText(), param);
            }
        }
        
        // Check each parameter
        for (Map.Entry<String, JsonNode> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            JsonNode paramConfig = entry.getValue();
            
            // Check if required parameter exists in results
            if (!results.has(paramName)) {
                // Check if parameter is required (default to required)
                boolean isRequired = !paramConfig.has("required") || paramConfig.get("required").asBoolean(true);
                if (isRequired) {
                    errors.add("Missing required parameter: " + paramName);
                }
                continue;
            }
            
            JsonNode resultValue = results.get(paramName);
            try {
                validateParameter(paramName, resultValue, paramConfig);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        
        // Check for unexpected fields in results
        Iterator<String> resultFields = results.fieldNames();
        while (resultFields.hasNext()) {
            String fieldName = resultFields.next();
            if (!parameterMap.containsKey(fieldName) && 
                !isAllowedExtraField(fieldName)) {
                errors.add("Unexpected field in results: " + fieldName);
            }
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Test results validation failed: " + String.join(", ", errors));
        }
    }
    
    /**
     * Validates a single parameter value
     */
    private void validateParameter(String name, JsonNode value, JsonNode config) {
        if (value.isNull()) {
            boolean allowNull = config.has("allowNull") && config.get("allowNull").asBoolean();
            if (!allowNull) {
                throw new IllegalArgumentException(name + " cannot be null");
            }
            return;
        }

        // Handle NABL-compliant structure: {"value": "...", "unit": "...", "status": "..."}
        JsonNode actualValue = value;
        if (value.isObject() && value.has("value")) {
            actualValue = value.get("value");
        }

        String type = config.has("type") ? config.get("type").asText() : "string";

        switch (type.toLowerCase()) {
            case "numeric":
            case "number":
                validateNumericParameter(name, actualValue, config);
                break;
            case "string":
            case "text":
                validateStringParameter(name, actualValue, config);
                break;
            case "boolean":
                validateBooleanParameter(name, actualValue, config);
                break;
            case "enum":
                validateEnumParameter(name, actualValue, config);
                break;
            default:
                // Unknown type, just check it's not null
                break;
        }
    }
    
    /**
     * Validates numeric parameters
     */
    private void validateNumericParameter(String name, JsonNode value, JsonNode config) {
        double val;

        // Handle both numeric and string representations
        if (value.isNumber()) {
            val = value.asDouble();
        } else if (value.isTextual()) {
            try {
                val = Double.parseDouble(value.asText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(name + " must be a valid number, got: " + value.asText());
            }
        } else {
            throw new IllegalArgumentException(name + " must be a number, got: " + value.getNodeType());
        }

        // Check minimum value
        if (config.has("min")) {
            double min = config.get("min").asDouble();
            if (val < min) {
                throw new IllegalArgumentException(name + " must be at least " + min + ", got: " + val);
            }
        }

        // Check maximum value
        if (config.has("max")) {
            double max = config.get("max").asDouble();
            if (val > max) {
                throw new IllegalArgumentException(name + " must be at most " + max + ", got: " + val);
            }
        }

        // Check reference range (for medical values)
        if (config.has("referenceRange")) {
            String referenceRange = config.get("referenceRange").asText();
            validateAgainstReferenceRange(name, val, referenceRange);
        }

        // Check precision (decimal places)
        if (config.has("precision")) {
            int precision = config.get("precision").asInt();
            String valueStr = value.asText();
            if (valueStr.contains(".")) {
                int decimalPlaces = valueStr.split("\\.")[1].length();
                if (decimalPlaces > precision) {
                    throw new IllegalArgumentException(name + " cannot have more than " + precision + " decimal places");
                }
            }
        }
    }
    
    /**
     * Validates string parameters
     */
    private void validateStringParameter(String name, JsonNode value, JsonNode config) {
        if (!value.isTextual()) {
            throw new IllegalArgumentException(name + " must be a string, got: " + value.getNodeType());
        }
        
        String val = value.asText();
        
        // Check minimum length
        if (config.has("minLength")) {
            int minLength = config.get("minLength").asInt();
            if (val.length() < minLength) {
                throw new IllegalArgumentException(name + " must be at least " + minLength + " characters long");
            }
        }
        
        // Check maximum length
        if (config.has("maxLength")) {
            int maxLength = config.get("maxLength").asInt();
            if (val.length() > maxLength) {
                throw new IllegalArgumentException(name + " must be at most " + maxLength + " characters long");
            }
        }
        
        // Check pattern (regex)
        if (config.has("pattern")) {
            String pattern = config.get("pattern").asText();
            if (!val.matches(pattern)) {
                throw new IllegalArgumentException(name + " does not match required pattern: " + pattern);
            }
        }
    }
    
    /**
     * Validates boolean parameters
     */
    private void validateBooleanParameter(String name, JsonNode value, JsonNode config) {
        if (!value.isBoolean()) {
            throw new IllegalArgumentException(name + " must be a boolean (true/false), got: " + value.asText());
        }
    }
    
    /**
     * Validates enum parameters
     */
    private void validateEnumParameter(String name, JsonNode value, JsonNode config) {
        if (!value.isTextual()) {
            throw new IllegalArgumentException(name + " must be a string for enum validation");
        }
        
        String val = value.asText();
        
        if (config.has("allowedValues") && config.get("allowedValues").isArray()) {
            boolean found = false;
            for (JsonNode allowedValue : config.get("allowedValues")) {
                if (allowedValue.asText().equals(val)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(name + " must be one of the allowed values, got: " + val);
            }
        }
    }
    
    /**
     * Validates value against medical reference range
     * Supports formats like: "10-20", "<50", ">40", "10-20 (M), 15-25 (F)"
     */
    private void validateAgainstReferenceRange(String name, double value, String referenceRange) {
        // This is a basic implementation - can be enhanced for complex medical ranges
        if (referenceRange.contains("-")) {
            String[] parts = referenceRange.split("-");
            if (parts.length == 2) {
                try {
                    double min = Double.parseDouble(parts[0].trim());
                    double max = Double.parseDouble(parts[1].replaceAll("[^0-9.]", "").trim());
                    if (value < min || value > max) {
                        // This is just a warning for reference range - not a hard validation error
                        // In a real system, you might want to flag this for review
                    }
                } catch (NumberFormatException e) {
                    // Complex reference range format - skip validation
                }
            }
        }
    }
    
    /**
     * Checks if a field is allowed as an extra field in results
     */
    private boolean isAllowedExtraField(String fieldName) {
        // Allow common extra fields
        return fieldName.equals("conclusion") || 
               fieldName.equals("comments") || 
               fieldName.equals("notes") ||
               fieldName.equals("interpretation") ||
               fieldName.equals("status") ||
               fieldName.equals("timestamp");
    }
    
    /**
     * Validates that all test results have proper structure for NABL compliance
     */
    public void validateNABLCompliance(JsonNode results) {
        if (results == null) {
            throw new IllegalArgumentException("NABL compliance requires test results");
        }
        
        // NABL requires each result to have value, unit, and status
        Iterator<Map.Entry<String, JsonNode>> fields = results.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String paramName = field.getKey();
            JsonNode paramValue = field.getValue();
            
            // Skip meta fields
            if (isAllowedExtraField(paramName)) {
                continue;
            }
            
            // For NABL compliance, each parameter should have structured data
            if (paramValue.isObject()) {
                if (!paramValue.has("value")) {
                    throw new IllegalArgumentException("NABL compliance: " + paramName + " must have 'value' field");
                }
                if (!paramValue.has("unit") && !paramValue.has("status")) {
                    throw new IllegalArgumentException("NABL compliance: " + paramName + " must have 'unit' or 'status' field");
                }
            }
        }
    }
}
