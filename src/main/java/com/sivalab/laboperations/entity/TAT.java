package com.sivalab.laboperations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tats")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TAT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "test_template_id", nullable = false, unique = true)
    private TestTemplate testTemplate;

    @Column(nullable = false)
    private int tatValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TATUnit tatUnit;

    public enum TATUnit {
        HOURS,
        DAYS
    }
}
