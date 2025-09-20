package com.sivalab.laboperations.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivalab.laboperations.dto.QualityControlRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QualityControlControllerTest extends LabOperationsApplicationIT {

    @Autowired
    private ObjectMapper objectMapper;

    private static Long createdTemplateId;

    @BeforeEach
    public void setUp() {
        // Create a test template to be used in the tests
        if (createdTemplateId == null) {
            Map<String, Object> templateRequest = new HashMap<>();
            templateRequest.put("name", "QC Test Template");
            templateRequest.put("description", "Test template for QC");
            templateRequest.put("basePrice", 100.0);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("param1", new HashMap<>() {{
                put("mean", 10.0);
                put("stdDev", 1.0);
            }});
            templateRequest.put("parameters", parameters);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/test-templates", templateRequest, Map.class);
            createdTemplateId = Long.valueOf(response.getBody().get("templateId").toString());
        }
    }

    @Test
    public void testCreateQualityControl() {
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 1");
        qcRequest.setControlLevel(1);
        qcRequest.setWestgardRules("1-3s,2-2s");

        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("controlName")).isEqualTo("QC Level 1");
    }

    @Test
    public void testGetQualityControlById() {
        // First create a QC to get
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 2");
        qcRequest.setControlLevel(2);
        qcRequest.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);
        Long qcId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now get the QC by ID
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/quality-controls/" + qcId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(qcId.intValue());
        assertThat(response.getBody().get("controlName")).isEqualTo("QC Level 2");
    }

    @Test
    public void testGetAllQualityControls() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "/api/quality-controls", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    public void testUpdateQualityControl() {
        // First create a QC to update
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 3");
        qcRequest.setControlLevel(3);
        qcRequest.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);
        Long qcId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now update the QC
        qcRequest.setControlName("Updated QC Level 3");
        HttpEntity<QualityControlRequest> entity = new HttpEntity<>(qcRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/quality-controls/" + qcId,
                HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("controlName")).isEqualTo("Updated QC Level 3");
    }

    @Test
    public void testDeleteQualityControl() {
        // First create a QC to delete
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 4");
        qcRequest.setControlLevel(4);
        qcRequest.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);
        Long qcId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now delete the QC
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/quality-controls/" + qcId,
                HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's gone
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/quality-controls/" + qcId, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testRecordQualityControlResult() {
        // First create a QC
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 5");
        qcRequest.setControlLevel(5);
        qcRequest.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);
        Long qcId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now record a result
        Map<String, Object> resultRequest = new HashMap<>();
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 10.1);
        resultRequest.put("results", results);
        resultRequest.put("passed", true);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/quality-controls/" + qcId + "/results",
                resultRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("passed")).isEqualTo(true);
    }

    @Test
    public void testGetQualityControlResultsByQcId() {
        // First create a QC and record a result
        QualityControlRequest qcRequest = new QualityControlRequest();
        qcRequest.setTestTemplateId(createdTemplateId);
        qcRequest.setControlName("QC Level 6");
        qcRequest.setControlLevel(6);
        qcRequest.setWestgardRules("1-3s");
        Map<String, String> frequency = new HashMap<>();
        frequency.put("type", "DAILY");
        qcRequest.setFrequency(objectMapper.valueToTree(frequency));

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-controls", qcRequest, Map.class);
        Long qcId = Long.valueOf(createResponse.getBody().get("id").toString());

        Map<String, Object> resultRequest = new HashMap<>();
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 10.2);
        resultRequest.put("results", results);
        resultRequest.put("passed", true);

        restTemplate.postForEntity(
                "/api/quality-controls/" + qcId + "/results",
                resultRequest, Map.class);

        // Now get the results
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "/api/quality-controls/" + qcId + "/results",
                Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }
}
