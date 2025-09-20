package com.sivalab.laboperations.integration;

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

public class ProficiencyTestControllerTest extends LabOperationsApplicationIT {

    private static Long createdTemplateId;

    @BeforeEach
    public void setUp() {
        // Create a test template to be used in the tests
        if (createdTemplateId == null) {
            Map<String, Object> templateRequest = new HashMap<>();
            templateRequest.put("name", "PT Test Template");
            templateRequest.put("description", "Test template for PT");
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
    public void testCreateProficiencyTest() {
        Map<String, Object> ptRequest = new HashMap<>();
        ptRequest.put("provider", "CAP");
        ptRequest.put("testDate", "2025-10-01");
        ptRequest.put("testTemplateId", createdTemplateId);

        Map<String, Double> results = new HashMap<>();
        results.put("param1", 10.5);
        ptRequest.put("results", results);
        ptRequest.put("passed", true);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/proficiency-tests", ptRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("provider")).isEqualTo("CAP");
    }

    @Test
    public void testGetProficiencyTestById() {
        // First create a PT to get
        Map<String, Object> ptRequest = new HashMap<>();
        ptRequest.put("provider", "NABL");
        ptRequest.put("testDate", "2025-11-01");
        ptRequest.put("testTemplateId", createdTemplateId);
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 9.8);
        ptRequest.put("results", results);
        ptRequest.put("passed", true);

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/proficiency-tests", ptRequest, Map.class);
        Long ptId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now get the PT by ID
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/proficiency-tests/" + ptId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(ptId.intValue());
        assertThat(response.getBody().get("provider")).isEqualTo("NABL");
    }

    @Test
    public void testGetAllProficiencyTests() {
        // First create a PT to get
        Map<String, Object> ptRequest = new HashMap<>();
        ptRequest.put("provider", "NABL");
        ptRequest.put("testDate", "2025-11-01");
        ptRequest.put("testTemplateId", createdTemplateId);
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 9.8);
        ptRequest.put("results", results);
        ptRequest.put("passed", true);

        restTemplate.postForEntity(
                "/api/proficiency-tests", ptRequest, Map.class);

        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "/api/proficiency-tests", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    public void testUpdateProficiencyTest() {
        // First create a PT to update
        Map<String, Object> ptRequest = new HashMap<>();
        ptRequest.put("provider", "CLIA");
        ptRequest.put("testDate", "2025-12-01");
        ptRequest.put("testTemplateId", createdTemplateId);
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 10.0);
        ptRequest.put("results", results);
        ptRequest.put("passed", true);

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/proficiency-tests", ptRequest, Map.class);
        Long ptId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now update the PT
        ptRequest.put("provider", "Updated CLIA");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(ptRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/proficiency-tests/" + ptId,
                HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("provider")).isEqualTo("Updated CLIA");
    }

    @Test
    public void testDeleteProficiencyTest() {
        // First create a PT to delete
        Map<String, Object> ptRequest = new HashMap<>();
        ptRequest.put("provider", "FDA");
        ptRequest.put("testDate", "2026-01-01");
        ptRequest.put("testTemplateId", createdTemplateId);
        Map<String, Double> results = new HashMap<>();
        results.put("param1", 10.0);
        ptRequest.put("results", results);
        ptRequest.put("passed", true);

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/proficiency-tests", ptRequest, Map.class);
        Long ptId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now delete the PT
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/proficiency-tests/" + ptId,
                HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's gone
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/proficiency-tests/" + ptId, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
