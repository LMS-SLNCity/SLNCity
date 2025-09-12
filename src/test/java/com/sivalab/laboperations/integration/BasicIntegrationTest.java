package com.sivalab.laboperations.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class BasicIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testApplicationStartsSuccessfully() {
        // Test that the application context loads successfully
        assertThat(port).isGreaterThan(0);
    }

    @Test
    public void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    public void testCreateVisit() {
        Map<String, Object> visitRequest = new HashMap<>();
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("name", "Test Patient");
        patientDetails.put("age", 30);
        patientDetails.put("phone", "1234567890");
        visitRequest.put("patientDetails", patientDetails);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits", visitRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("visitId")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");
    }

    @Test
    public void testCreateTestTemplate() {
        Map<String, Object> templateRequest = new HashMap<>();
        templateRequest.put("name", "Test Template");
        templateRequest.put("description", "A test template");
        templateRequest.put("basePrice", 100.0);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("testField", "testValue");
        templateRequest.put("parameters", parameters);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/test-templates", templateRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("templateId")).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Test Template");
    }

    @Test
    public void testGetAllVisits() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void testGetAllTestTemplates() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void testGetAllBills() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void testNotFoundEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
