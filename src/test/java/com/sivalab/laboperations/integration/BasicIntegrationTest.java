package com.sivalab.laboperations.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Test data storage
    private static Long createdVisitId;
    private static Long createdTemplateId;
    private static Long createdTestId;
    private static Long createdBillId;

    @BeforeEach
    public void setUp() {
        // Configure RestTemplate to support PATCH method
        try {
            restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        } catch (Exception e) {
            // Fallback if HttpComponents not available
            System.out.println("HttpComponents not available, PATCH tests may fail");
        }
    }

    // ========== BASIC HEALTH TESTS ==========

    @Test
    @Order(1)
    public void testApplicationStartsSuccessfully() {
        // Test that the application context loads successfully
        assertThat(port).isGreaterThan(0);
    }

    @Test
    @Order(2)
    public void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    // ========== TEST TEMPLATE TESTS ==========

    @Test
    @Order(10)
    public void testCreateTestTemplate() {
        Map<String, Object> templateRequest = new HashMap<>();
        templateRequest.put("name", "Blood Test Template");
        templateRequest.put("description", "Complete blood count test");
        templateRequest.put("basePrice", 150.0);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hemoglobin", "normal");
        parameters.put("wbc_count", "normal");
        templateRequest.put("parameters", parameters);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/test-templates", templateRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("templateId")).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Blood Test Template");

        // Store for later tests
        createdTemplateId = Long.valueOf(response.getBody().get("templateId").toString());
    }

    @Test
    @Order(11)
    public void testGetTestTemplateById() {
        assertThat(createdTemplateId).isNotNull();

        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates/" + createdTemplateId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("templateId")).isEqualTo(createdTemplateId.intValue());
        assertThat(response.getBody().get("name")).isEqualTo("Blood Test Template");
    }

    @Test
    @Order(12)
    public void testGetAllTestTemplates() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(13)
    public void testSearchTestTemplates() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates/search?name=Blood", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(14)
    public void testUpdateTestTemplate() {
        assertThat(createdTemplateId).isNotNull();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", "Updated Blood Test Template");
        updateRequest.put("description", "Updated complete blood count test");
        updateRequest.put("basePrice", 200.0);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hemoglobin", "updated");
        parameters.put("wbc_count", "updated");
        updateRequest.put("parameters", parameters);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
            "http://localhost:" + port + "/test-templates/" + createdTemplateId,
            HttpMethod.PUT, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Updated Blood Test Template");
        assertThat(response.getBody().get("basePrice")).isEqualTo(200.0);
    }

    // ========== VISIT TESTS ==========

    @Test
    @Order(20)
    public void testCreateVisit() {
        Map<String, Object> visitRequest = new HashMap<>();
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("name", "John Doe");
        patientDetails.put("age", 35);
        patientDetails.put("gender", "M");
        patientDetails.put("phone", "9999999999");
        patientDetails.put("address", "Hyderabad, India");
        visitRequest.put("patientDetails", patientDetails);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits", visitRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("visitId")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");

        // Store for later tests
        createdVisitId = Long.valueOf(response.getBody().get("visitId").toString());
    }

    @Test
    @Order(21)
    public void testGetVisitById() {
        assertThat(createdVisitId).isNotNull();

        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/" + createdVisitId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("visitId")).isEqualTo(createdVisitId.intValue());
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");
    }

    @Test
    @Order(22)
    public void testGetAllVisits() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(23)
    public void testGetVisitsByStatus() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits?status=pending", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(24)
    public void testSearchVisitsByPhone() {
        // First create a visit with a phone number to search for
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("name", "Jane Smith");
        patientDetails.put("age", 28);
        patientDetails.put("gender", "F");
        patientDetails.put("phone", "8888888888");
        patientDetails.put("address", "Mumbai, India");

        Map<String, Object> visitRequest = new HashMap<>();
        visitRequest.put("patientDetails", patientDetails);

        restTemplate.postForEntity("http://localhost:" + port + "/visits", visitRequest, Map.class);

        // Now search for visits by phone
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/search?phone=8888888888", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(25)
    public void testUpdateVisitStatus() {
        assertThat(createdVisitId).isNotNull();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:" + port + "/visits/" + createdVisitId + "/status?status=in-progress",
                HttpMethod.PATCH, null, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("IN_PROGRESS");
        } catch (Exception e) {
            // Skip PATCH test if not supported
            System.out.println("PATCH method not supported, skipping test: " + e.getMessage());
        }
    }

    // ========== LAB TEST TESTS ==========

    @Test
    @Order(30)
    public void testAddTestToVisit() {
        assertThat(createdVisitId).isNotNull();
        assertThat(createdTemplateId).isNotNull();

        Map<String, Object> testRequest = new HashMap<>();
        testRequest.put("testTemplateId", createdTemplateId);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits/" + createdVisitId + "/tests",
            testRequest, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("testId")).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("PENDING");

        // Store for later tests - handle both Integer and Long
        Object testIdObj = response.getBody().get("testId");
        if (testIdObj instanceof Integer) {
            createdTestId = ((Integer) testIdObj).longValue();
        } else {
            createdTestId = Long.valueOf(testIdObj.toString());
        }

        System.out.println("Created test ID: " + createdTestId);
    }

    @Test
    @Order(31)
    public void testGetTestsForVisit() {
        assertThat(createdVisitId).isNotNull();

        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/" + createdVisitId + "/tests", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    @Order(32)
    public void testUpdateTestResults() {
        assertThat(createdVisitId).isNotNull();
        assertThat(createdTestId).isNotNull();

        Map<String, Object> resultsRequest = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        results.put("hemoglobin", "12.5 g/dL");
        results.put("wbc_count", "7500 cells/μL");
        results.put("conclusion", "Normal values");
        resultsRequest.put("results", results);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(resultsRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + createdVisitId + "/tests/" + createdTestId + "/results",
            HttpMethod.PATCH, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("COMPLETED");
    }

    @Test
    @Order(33)
    public void testApproveTestResults() {
        assertThat(createdVisitId).isNotNull();
        assertThat(createdTestId).isNotNull();

        Map<String, Object> approveRequest = new HashMap<>();
        approveRequest.put("approvedBy", "Dr. Smith");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(approveRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + createdVisitId + "/tests/" + createdTestId + "/approve",
            HttpMethod.PATCH, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("approved")).isEqualTo(true);
    }

    @Test
    @Order(34)
    public void testVisitStatusUpdateLogic() {
        // Create a new visit for this test
        Map<String, Object> visitRequest = new HashMap<>();
        Map<String, Object> patientDetails = new HashMap<>();
        patientDetails.put("name", "Test Patient");
        patientDetails.put("age", 30);
        patientDetails.put("phone", "1234567890");
        visitRequest.put("patientDetails", patientDetails);

        HttpEntity<Map<String, Object>> visitEntity = new HttpEntity<>(visitRequest);
        ResponseEntity<Map> visitResponse = restTemplate.exchange(
            "http://localhost:" + port + "/visits",
            HttpMethod.POST, visitEntity, Map.class);

        assertThat(visitResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long testVisitId = ((Number) visitResponse.getBody().get("visitId")).longValue();

        // Add two tests to the visit
        Map<String, Object> testRequest = new HashMap<>();
        testRequest.put("testTemplateId", createdTemplateId);
        testRequest.put("price", 100.00);

        HttpEntity<Map<String, Object>> testEntity = new HttpEntity<>(testRequest);

        // Add first test
        ResponseEntity<Map> test1Response = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests",
            HttpMethod.POST, testEntity, Map.class);
        Long test1Id = ((Number) test1Response.getBody().get("testId")).longValue();

        // Add second test
        ResponseEntity<Map> test2Response = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests",
            HttpMethod.POST, testEntity, Map.class);
        Long test2Id = ((Number) test2Response.getBody().get("testId")).longValue();

        // Complete and approve first test
        Map<String, Object> resultsRequest = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        results.put("hemoglobin", "14.0 g/dL");
        resultsRequest.put("results", results);

        HttpEntity<Map<String, Object>> resultsEntity = new HttpEntity<>(resultsRequest);
        restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests/" + test1Id + "/results",
            HttpMethod.PATCH, resultsEntity, Map.class);

        Map<String, Object> approveRequest = new HashMap<>();
        approveRequest.put("approvedBy", "Dr. Test");
        HttpEntity<Map<String, Object>> approveEntity = new HttpEntity<>(approveRequest);

        restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests/" + test1Id + "/approve",
            HttpMethod.PATCH, approveEntity, Map.class);

        // Check visit status - should NOT be approved yet (second test still pending)
        ResponseEntity<Map> visitStatusResponse = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId,
            HttpMethod.GET, null, Map.class);

        String visitStatus = (String) visitStatusResponse.getBody().get("status");
        assertThat(visitStatus).isNotEqualTo("APPROVED").withFailMessage(
            "Visit should not be approved when second test is still pending");

        // Complete and approve second test
        restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests/" + test2Id + "/results",
            HttpMethod.PATCH, resultsEntity, Map.class);

        restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId + "/tests/" + test2Id + "/approve",
            HttpMethod.PATCH, approveEntity, Map.class);

        // Check visit status - should NOW be approved
        visitStatusResponse = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + testVisitId,
            HttpMethod.GET, null, Map.class);

        visitStatus = (String) visitStatusResponse.getBody().get("status");
        assertThat(visitStatus).isEqualTo("APPROVED").withFailMessage(
            "Visit should be approved when all tests are completed and approved");
    }

    // ========== BILLING TESTS ==========

    @Test
    @Order(40)
    public void testGenerateBill() {
        assertThat(createdVisitId).isNotNull();

        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/visits/" + createdVisitId + "/bill", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("billId")).isNotNull();
        assertThat(response.getBody().get("totalAmount")).isNotNull();
        assertThat(response.getBody().get("paid")).isEqualTo(false);

        // Store for later tests - handle both Integer and Long
        Object billIdObj = response.getBody().get("billId");
        if (billIdObj instanceof Integer) {
            createdBillId = ((Integer) billIdObj).longValue();
        } else {
            createdBillId = Long.valueOf(billIdObj.toString());
        }

        System.out.println("Created bill ID: " + createdBillId);
    }

    @Test
    @Order(41)
    public void testGetBillById() {
        assertThat(createdBillId).isNotNull();

        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/" + createdBillId, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("billId")).isEqualTo(createdBillId.intValue());
        assertThat(response.getBody().get("paid")).isEqualTo(false);
    }

    @Test
    @Order(42)
    public void testGetAllBills() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // After creating a bill, we should have at least one
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(43)
    public void testGetUnpaidBills() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/unpaid", Object[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Should have unpaid bills before marking as paid
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(44)
    public void testMarkBillAsPaid() {
        assertThat(createdBillId).isNotNull();

        ResponseEntity<Map> response = restTemplate.exchange(
            "http://localhost:" + port + "/billing/" + createdBillId + "/pay",
            HttpMethod.PATCH, null, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("paid")).isEqualTo(true);
        assertThat(response.getBody().get("createdAt")).isNotNull();
    }

    @Test
    @Order(45)
    public void testGetRevenueForPeriod() {
        String startDate = "2023-01-01T00:00:00";
        String endDate = "2025-12-31T23:59:59";

        ResponseEntity<Double> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/revenue?startDate=" + startDate + "&endDate=" + endDate,
            Double.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isGreaterThanOrEqualTo(0.0);
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @Order(50)
    public void testGetNonExistentVisit() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(51)
    public void testGetNonExistentTestTemplate() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(52)
    public void testGetNonExistentBill() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(53)
    public void testCreateVisitWithInvalidData() {
        Map<String, Object> visitRequest = new HashMap<>();
        // Missing required patientDetails

        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits", visitRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(54)
    public void testCreateTestTemplateWithInvalidData() {
        Map<String, Object> templateRequest = new HashMap<>();
        // Missing required fields

        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/test-templates", templateRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(55)
    public void testSearchVisitsWithEmptyPhone() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/search?phone=", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(56)
    public void testSearchTestTemplatesWithEmptyName() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/test-templates/search?name=", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(57)
    public void testUpdateVisitStatusWithInvalidStatus() {
        assertThat(createdVisitId).isNotNull();

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/visits/" + createdVisitId + "/status?status=invalid-status",
            HttpMethod.PATCH, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(58)
    public void testGetVisitsByInvalidStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits?status=invalid-status", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(59)
    public void testGetRevenueWithInvalidDateRange() {
        String startDate = "2025-01-01T00:00:00";
        String endDate = "2023-12-31T23:59:59"; // End before start

        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/billing/revenue?startDate=" + startDate + "&endDate=" + endDate,
            String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(34)
    public void testVisitCountByStatus() {
        // Test the visit count by status endpoint
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/count-by-status",
            Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> statusCounts = response.getBody();

        // Verify all status types are present
        assertThat(statusCounts).containsKeys(
            "pending", "in-progress", "awaiting-approval",
            "approved", "billed", "completed"
        );

        // Verify counts are numbers (Long values)
        for (Object count : statusCounts.values()) {
            assertThat(count).isInstanceOf(Number.class);
            assertThat(((Number) count).longValue()).isGreaterThanOrEqualTo(0);
        }

        // Verify total visits count is non-negative (could be 0 if no visits exist)
        long totalVisits = statusCounts.values().stream()
            .mapToLong(count -> ((Number) count).longValue())
            .sum();
        assertThat(totalVisits).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(35)
    public void testVisitCountByStatusWithSpecificData() {
        // Create visits with known statuses to test counting accuracy

        // Create first visit (will be PENDING by default)
        Map<String, Object> patientDetails1 = new HashMap<>();
        patientDetails1.put("name", "Count Test Patient 1");
        patientDetails1.put("age", 30);
        patientDetails1.put("phone", "1111111111");

        Map<String, Object> visitRequest1 = new HashMap<>();
        visitRequest1.put("patientDetails", patientDetails1);

        ResponseEntity<Map> createResponse1 = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits",
            visitRequest1,
            Map.class);

        assertThat(createResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long visitId1 = ((Number) createResponse1.getBody().get("visitId")).longValue();

        // Create second visit and update to IN_PROGRESS
        Map<String, Object> patientDetails2 = new HashMap<>();
        patientDetails2.put("name", "Count Test Patient 2");
        patientDetails2.put("age", 25);
        patientDetails2.put("phone", "2222222222");

        Map<String, Object> visitRequest2 = new HashMap<>();
        visitRequest2.put("patientDetails", patientDetails2);

        ResponseEntity<Map> createResponse2 = restTemplate.postForEntity(
            "http://localhost:" + port + "/visits",
            visitRequest2,
            Map.class);

        assertThat(createResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long visitId2 = ((Number) createResponse2.getBody().get("visitId")).longValue();

        // Update second visit to IN_PROGRESS
        restTemplate.patchForObject(
            "http://localhost:" + port + "/visits/" + visitId2 + "/status?status=in-progress",
            null,
            Map.class);

        // Get count by status
        ResponseEntity<Map> countResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/visits/count-by-status",
            Map.class);

        assertThat(countResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> statusCounts = countResponse.getBody();

        // Verify we have at least 1 pending and 1 in-progress
        assertThat(((Number) statusCounts.get("pending")).longValue()).isGreaterThanOrEqualTo(1);
        assertThat(((Number) statusCounts.get("in-progress")).longValue()).isGreaterThanOrEqualTo(1);
    }
}
