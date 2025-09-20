package com.sivalab.laboperations.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_controls")
public class QualityControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_template_id", nullable = false)
    private TestTemplate testTemplate;

    @Column(nullable = false)
    private String controlName;

    @Column(nullable = false)
    private int controlLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private JsonNode frequency;

    @Column(nullable = false)
    private String westgardRules; // e.g., "1_3s,2_2s,R_4s"

    private LocalDateTime createdAt;

    private LocalDateTime nextDueDate;

    public QualityControl() {
        this.createdAt = LocalDateTime.now();
    }

    public QualityControl(TestTemplate testTemplate, String controlName, int controlLevel, JsonNode frequency, String westgardRules) {
        this();
        this.testTemplate = testTemplate;
        this.controlName = controlName;
        this.controlLevel = controlLevel;
        this.frequency = frequency;
        this.westgardRules = westgardRules;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestTemplate getTestTemplate() {
        return testTemplate;
    }

    public void setTestTemplate(TestTemplate testTemplate) {
        this.testTemplate = testTemplate;
    }

    public String getControlName() {
        return controlName;
    }

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public int getControlLevel() {
        return controlLevel;
    }

    public void setControlLevel(int controlLevel) {
        this.controlLevel = controlLevel;
    }

    public JsonNode getFrequency() {
        return frequency;
    }

    public void setFrequency(JsonNode frequency) {
        this.frequency = frequency;
    }

    public String getWestgardRules() {
        return westgardRules;
    }

    public void setWestgardRules(String westgardRules) {
        this.westgardRules = westgardRules;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDateTime nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
}
