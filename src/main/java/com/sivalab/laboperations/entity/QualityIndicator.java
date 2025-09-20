package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "quality_indicators")
public class QualityIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "indicator_value")
    private String indicatorValue;

    @Column(nullable = false)
    private LocalDate recordedAt;

    public QualityIndicator() {
        this.recordedAt = LocalDate.now();
    }

    public QualityIndicator(String name, String indicatorValue) {
        this();
        this.name = name;
        this.indicatorValue = indicatorValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(String indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public LocalDate getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDate recordedAt) {
        this.recordedAt = recordedAt;
    }
}
