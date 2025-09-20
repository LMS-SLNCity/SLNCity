package com.sivalab.laboperations.integration;

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

public class QualityIndicatorControllerTest extends LabOperationsApplicationIT {

    @Test
    public void testCreateQualityIndicator() {
        Map<String, String> qiRequest = new HashMap<>();
        qiRequest.put("name", "TAT");
        qiRequest.put("indicatorValue", "95%");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/quality-indicators", qiRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("TAT");
    }

    @Test
    public void testGetQualityIndicatorById() {
        // First create a QI to get
        Map<String, String> qiRequest = new HashMap<>();
        qiRequest.put("name", "Rejection Rate");
        qiRequest.put("indicatorValue", "1%");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-indicators", qiRequest, Map.class);
        Long qiId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now get the QI by ID
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/quality-indicators/" + qiId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isEqualTo(qiId.intValue());
        assertThat(response.getBody().get("name")).isEqualTo("Rejection Rate");
    }

    @Test
    public void testGetAllQualityIndicators() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
                "/api/quality-indicators", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    public void testUpdateQualityIndicator() {
        // First create a QI to update
        Map<String, String> qiRequest = new HashMap<>();
        qiRequest.put("name", "TAT");
        qiRequest.put("indicatorValue", "90%");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-indicators", qiRequest, Map.class);
        Long qiId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now update the QI
        qiRequest.put("indicatorValue", "95%");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(qiRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/quality-indicators/" + qiId,
                HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("indicatorValue")).isEqualTo("95%");
    }

    @Test
    public void testDeleteQualityIndicator() {
        // First create a QI to delete
        Map<String, String> qiRequest = new HashMap<>();
        qiRequest.put("name", "TAT");
        qiRequest.put("indicatorValue", "90%");

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/quality-indicators", qiRequest, Map.class);
        Long qiId = Long.valueOf(createResponse.getBody().get("id").toString());

        // Now delete the QI
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/quality-indicators/" + qiId,
                HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


        // Verify it's gone
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/quality-indicators/" + qiId, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
