package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "personnel")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String qualifications;

    @ElementCollection
    private List<String> trainingRecords;

    @Temporal(TemporalType.DATE)
    private Date competencyAssessmentDate;

    private String competencyAssessmentResult;
}
