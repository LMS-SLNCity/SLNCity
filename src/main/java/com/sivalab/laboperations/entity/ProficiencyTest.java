package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "proficiency_tests")
public class ProficiencyTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private LocalDate testDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_template_id", nullable = false)
    private TestTemplate testTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private JsonNode results;

    @Column(nullable = false)
    private boolean passed;

    public ProficiencyTest() {
    }

    public ProficiencyTest(String provider, LocalDate testDate, TestTemplate testTemplate, JsonNode results, boolean passed) {
        this.provider = provider;
        this.testDate = testDate;
        this.testTemplate = testTemplate;
        this.results = results;
        this.passed = passed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public TestTemplate getTestTemplate() {
        return testTemplate;
    }

    public void setTestTemplate(TestTemplate testTemplate) {
        this.testTemplate = testTemplate;
    }

    public JsonNode getResults() {
        return results;
    }

    public void setResults(JsonNode results) {
        this.results = results;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
