package com.sivalab.laboperations.integration;

import com.sivalab.laboperations.entity.TAT;
import com.sivalab.laboperations.entity.TestTemplate;
import com.sivalab.laboperations.repository.TATRepository;
import com.sivalab.laboperations.repository.TestTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TATControllerTest extends LabOperationsApplicationIT {

    @Autowired
    private TestTemplateRepository testTemplateRepository;

    @Autowired
    private TATRepository tatRepository;

    private TestTemplate testTemplate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        tatRepository.deleteAll();
        testTemplateRepository.deleteAll();
        testTemplate = new TestTemplate("TAT Test Template", "Test template for TAT", null, new BigDecimal("50.00"));
        testTemplate = testTemplateRepository.save(testTemplate);
    }

    @Test
    void shouldCreateTAT() {
        Map<String, Object> request = Map.of(
                "testTemplateId", testTemplate.getTemplateId(),
                "tatValue", 24,
                "tatUnit", "HOURS"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/tats", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("tatValue")).isEqualTo(24);
        assertThat(response.getBody().get("tatUnit")).isEqualTo("HOURS");
    }

    @Test
    void shouldGetTATByTestTemplateId() {
        TAT tat = new TAT(null, testTemplate, 48, TAT.TATUnit.HOURS);
        tatRepository.save(tat);

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/tats/test-template/{id}", Map.class, testTemplate.getTemplateId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("tatValue")).isEqualTo(48);
    }

    @Test
    void shouldUpdateTAT() {
        TAT tat = new TAT(null, testTemplate, 2, TAT.TATUnit.DAYS);
        tat = tatRepository.save(tat);

        Map<String, Object> request = Map.of(
                "testTemplateId", testTemplate.getTemplateId(),
                "tatValue", 3,
                "tatUnit", "DAYS"
        );

        ResponseEntity<Map> response = restTemplate.exchange("/api/tats/{id}", HttpMethod.PUT, new HttpEntity<>(request), Map.class, tat.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("tatValue")).isEqualTo(3);
    }

    @Test
    void shouldDeleteTAT() {
        TAT tat = new TAT(null, testTemplate, 1, TAT.TATUnit.DAYS);
        tat = tatRepository.save(tat);

        ResponseEntity<Void> response = restTemplate.exchange("/api/tats/{id}", HttpMethod.DELETE, null, Void.class, tat.getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
